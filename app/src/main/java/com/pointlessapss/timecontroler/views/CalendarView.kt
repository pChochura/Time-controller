package com.pointlessapss.timecontroler.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.OverScroller
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.NestedScrollingChild
import androidx.core.view.ViewCompat
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.models.Event
import com.pointlessapss.timecontroler.models.Padding
import com.pointlessapss.timecontroler.utils.dp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.round
import kotlin.math.sign

class CalendarView(
	context: Context,
	attrs: AttributeSet,
	defStyleAttr: Int,
	defStyleRes: Int
) : View(context, attrs, defStyleAttr, defStyleRes), NestedScrollingChild {

	private var onMonthChangeListener: ((currentMonth: Calendar) -> Unit)? = null
	private var onGetMonthEventListener: ((currentMonth: Calendar) -> List<Event>)? = null

	private lateinit var scroller: OverScroller
	private lateinit var gestureDetector: GestureDetector

	private var isTouching = false

	private val swipeMinDistance = 5
	private val swipeThresholdVelocity = 300
	private val snappingThreshold = 2
	private val scrollDuration = 300
	private val numberOfDays = 7
	private val numberOfRows = 6
	private val radiusEvents = 3.dp
	private val formatLabel = SimpleDateFormat("EEE", Locale.getDefault())

	private var firstDayOfWeek = Calendar.MONDAY

	private lateinit var today: Calendar
	private lateinit var currentMonth: Calendar
	private lateinit var selectedDay: Calendar

	private var offset = 0

	private var mWidth = 0f
	private var mHeight = 0f
	private var mDayWidth = 0f
	private var mDayHeight = 0f

	private val paddingLabels = Padding(verticalPadding = 5.dp)
	private val paddingEvents = Padding(2.dp)

	private lateinit var paintBg: Paint
	private lateinit var paintSelectedDay: Paint
	private lateinit var paintToday: Paint
	private lateinit var paintEvents: Paint
	private lateinit var paintLabels: TextPaint
	private lateinit var paintCurrentMonth: TextPaint
	private lateinit var paintOtherMonth: TextPaint

	@ColorInt private var colorBg: Int = 0
	@ColorInt private var colorSelectedDay: Int = 0
	@ColorInt private var colorToday: Int = 0
	@ColorInt private var colorTextToday: Int = 0
	@ColorInt private var colorLabels: Int = 0
	@ColorInt private var colorCurrentMonth: Int = 0
	@ColorInt private var colorOtherMonth: Int = 0
	@ColorInt private var colorDefaultEvent: Int = 0

	private var textSizeLabels = 12f
	private var textSizeDays = 20f

	private lateinit var textTypeface: Typeface

	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

	constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0, 0)

	@SuppressLint("ClickableViewAccessibility")
	override fun onTouchEvent(event: MotionEvent): Boolean {
		when {
			event.action == MotionEvent.ACTION_UP -> {
				isTouching = false
				invalidate()
			}
			event.action == MotionEvent.ACTION_DOWN -> {
				if (!scroller.isFinished) {
					return false
				}
				isTouching = true
			}
		}

		return gestureDetector.onTouchEvent(event)
	}

	fun setOnMonthChangeListener(onMonthChangeListener: (currentMonth: Calendar) -> Unit) {
		this.onMonthChangeListener = onMonthChangeListener
	}

	fun setGetMonthEventListener(onGetMonthEventListener: (currentMonth: Calendar) -> List<Event>) {
		this.onGetMonthEventListener = onGetMonthEventListener
	}

	init {
		init(context, attrs)
	}

	private fun calculateSize() {
		post {
			mWidth = width.toFloat()
			mHeight = height.toFloat()

			mDayWidth = mWidth / numberOfDays
			mDayHeight = (mHeight - textSizeLabels - paddingLabels.vertical) / numberOfRows
		}
	}

	private fun init(context: Context, attrs: AttributeSet) {
		val a = context.theme.obtainStyledAttributes(attrs, R.styleable.CalendarView, 0, 0)

		obtainStyles(a, context)

		paintBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
			color = colorBg
		}
		paintSelectedDay = Paint(Paint.ANTI_ALIAS_FLAG).apply {
			color = colorSelectedDay
		}
		paintToday = Paint(Paint.ANTI_ALIAS_FLAG).apply {
			color = colorToday
		}
		paintLabels = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
			color = colorLabels
			textSize = textSizeLabels
			typeface = textTypeface
		}
		paintCurrentMonth = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
			color = colorCurrentMonth
			textSize = textSizeDays
			typeface = textTypeface
		}
		paintOtherMonth = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
			color = colorOtherMonth
			textSize = textSizeDays
			typeface = textTypeface
		}
		paintEvents = Paint(Paint.ANTI_ALIAS_FLAG).apply {
			color = colorDefaultEvent
		}

		today = Calendar.getInstance()
		today.firstDayOfWeek = firstDayOfWeek
		currentMonth = today.clone() as Calendar
		selectedDay = currentMonth.clone() as Calendar

		prepareGestureDetector(context)

		calculateSize()
	}

	private fun obtainStyles(a: TypedArray?, context: Context) {
		a?.also {
			try {
				colorBg = a.getColor(
					R.styleable.CalendarView_cv_color_bg,
					ContextCompat.getColor(context, android.R.color.white)
				)
				colorSelectedDay = a.getColor(
					R.styleable.CalendarView_cv_color_selected_day,
					ContextCompat.getColor(context, R.color.colorAccentLight)
				)
				colorToday = a.getColor(
					R.styleable.CalendarView_cv_color_today,
					ContextCompat.getColor(context, R.color.colorAccent)
				)
				colorTextToday = a.getColor(
					R.styleable.CalendarView_cv_color_text_today,
					ContextCompat.getColor(context, R.color.colorText3)
				)
				colorLabels = a.getColor(
					R.styleable.CalendarView_cv_color_text_labels,
					ContextCompat.getColor(context, R.color.colorText1)
				)
				colorCurrentMonth = a.getColor(
					R.styleable.CalendarView_cv_color_text_current_month,
					ContextCompat.getColor(context, R.color.colorText1)
				)
				colorOtherMonth = a.getColor(
					R.styleable.CalendarView_cv_color_text_other_month,
					ContextCompat.getColor(context, R.color.colorText2)
				)
				textSizeLabels = a.getDimension(
					R.styleable.CalendarView_cv_font_size_labels,
					textSizeLabels
				)
				textSizeDays = a.getDimension(
					R.styleable.CalendarView_cv_font_size_days,
					textSizeDays
				)
				textTypeface = ResourcesCompat.getFont(
					context,
					a.getResourceId(R.styleable.CalendarView_cv_font_typeface, R.font.lato)
				)!!
				colorDefaultEvent = ContextCompat.getColor(getContext(), R.color.colorTaskDefault)
			} catch (ex: Exception) {
				Log.d(javaClass.name, ex.message!!)
			}

			a.recycle()
		}
	}

	private fun prepareGestureDetector(context: Context) {
		scroller = OverScroller(context, LinearInterpolator())
		gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
			override fun onDown(e: MotionEvent): Boolean {
				scroller.forceFinished(true)
				startNestedScroll(ViewCompat.SCROLL_AXIS_HORIZONTAL or ViewCompat.SCROLL_AXIS_VERTICAL)
				return true
			}

			override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
				offset -= distanceX.toInt()
				invalidate()
				dispatchNestedScroll(distanceX.toInt(), distanceY.toInt(), 0, 0, null)
				return true
			}

			override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
				if (e1.x - e2.x > swipeMinDistance && abs(velocityX) > swipeThresholdVelocity) {
					scroller.startScroll(offset, 0, -(offset + mWidth).toInt(), 0, scrollDuration)
				} else if (e2.x - e1.x > swipeMinDistance && abs(velocityX) > swipeThresholdVelocity) {
					scroller.startScroll(offset, 0, -(offset - mWidth).toInt(), 0, scrollDuration)
				}
				dispatchNestedFling(velocityX, velocityY, true)
				return true
			}

			override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
				val posX = (e.x / mDayWidth).toInt()
				val posY = ((e.y - paddingLabels.vertical - textSizeLabels) / mDayHeight).toInt()
				val dayOffset = posY * numberOfDays + posX
				selectedDay.timeInMillis = currentMonth.timeInMillis
				selectedDay.set(Calendar.WEEK_OF_MONTH, 1)
				selectedDay.set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
				selectedDay.add(Calendar.DAY_OF_MONTH, dayOffset)
				invalidate()
				return true
			}
		})
	}

	private fun updateOffset() {
		if (scroller.computeScrollOffset()) {
			offset = scroller.currX
		}
		if (!isTouching) {
			val wantedOffset = round(offset / mWidth) * mWidth
			val diff = offset - wantedOffset
			if (abs(diff) >= snappingThreshold) {
				if (scroller.isFinished) {
					scroller.startScroll(offset, 0, (-diff).toInt(), 0, scrollDuration)
				}
				invalidate()
			} else {
				if (scroller.isFinished && offset != 0) {
					currentMonth.add(Calendar.MONTH, -offset.sign)
					offset = 0
					onMonthChangeListener?.invoke(currentMonth)
					invalidate()
				}
			}
		}
	}

	override fun onDraw(canvas: Canvas) {
		updateOffset()

		drawBackground(canvas)
		drawLabels(canvas)
		drawMonth(canvas, 0)
		drawMonth(canvas, -1)
		drawMonth(canvas, 1)
	}

	private fun drawBackground(canvas: Canvas) {
		canvas.drawRect(0f, 0f, mWidth, mHeight, paintBg)
	}

	private fun drawMonth(canvas: Canvas, monthOffset: Int) {
		val month = currentMonth.clone() as Calendar
		month.add(Calendar.MONTH, monthOffset)
		drawDays(canvas, month, offset + monthOffset * mWidth)
		drawEvents(canvas, month, offset + monthOffset * mWidth)
	}

	private fun drawLabels(canvas: Canvas) {
		val day = today.clone() as Calendar
		day.set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
		for (i in 0 until numberOfDays) {
			val x = i * mDayWidth
			drawLabel(canvas, day, x)

			day.add(Calendar.DAY_OF_MONTH, 1)
		}
	}

	private fun drawLabel(canvas: Canvas, day: Calendar, x: Float) {
		val text = formatLabel.format(day.time).toUpperCase()
		val offset = (mDayWidth - paintLabels.measureText(text)) / 2
		canvas.drawText(text, x + offset, paintLabels.textSize + paddingLabels.top, paintLabels)
	}

	private fun drawDays(canvas: Canvas, month: Calendar, offset: Float) {
		val day = month.clone() as Calendar
		day.set(Calendar.WEEK_OF_MONTH, 1)
		day.set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
		for (j in 0 until numberOfRows) {
			for (i in 0 until numberOfDays) {
				val currentMonth = day.get(Calendar.MONTH) == month.get(Calendar.MONTH)
				val currentDay = day.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
						day.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
				val selectedDay = day.get(Calendar.YEAR) == selectedDay.get(Calendar.YEAR) &&
						day.get(Calendar.DAY_OF_YEAR) == selectedDay.get(Calendar.DAY_OF_YEAR)
				val text = day.get(Calendar.DAY_OF_MONTH).toString()
				val paint = if (currentMonth) paintCurrentMonth else paintOtherMonth
				val verticalOffset = mDayHeight / 2
				val horizontalOffset = (mDayWidth - paint.measureText(text)) / 2
				val x = i * mDayWidth + offset
				val y = j * mDayHeight + paddingLabels.vertical + textSizeLabels

				if (selectedDay) {
					paintSelectedDay.color = colorSelectedDay
					canvas.drawRect(x, y, x + mDayWidth, y + mDayHeight, paintSelectedDay)
					paintSelectedDay.color = ColorUtils.blendARGB(colorSelectedDay, Color.BLACK, 0.5f)
					canvas.drawLine(x + 2, y + 2, x + mDayWidth - 2, y + 2, paintSelectedDay)
					canvas.drawLine(x + mDayWidth - 2, y + 2, x + mDayWidth - 2, y + mDayHeight - 2, paintSelectedDay)
					canvas.drawLine(x + mDayWidth - 2, y + mDayHeight - 2, x + 2, y + mDayHeight - 2, paintSelectedDay)
					canvas.drawLine(x + 2, y + mDayHeight - 2, x + 2, y + 2, paintSelectedDay)
				}

				if (currentDay) {
					canvas.drawCircle(
						x + mDayWidth / 2,
						y + verticalOffset - paint.textSize / 4,
						mDayHeight / 4,
						paintToday
					)
					paintCurrentMonth.color = colorTextToday
				} else {
					paintCurrentMonth.color = colorCurrentMonth
				}

				canvas.drawText(text, x + horizontalOffset, y + verticalOffset, paint)

				day.add(Calendar.DAY_OF_MONTH, 1)
			}
		}
	}

	private fun drawEvents(canvas: Canvas, month: Calendar, offset: Float) {
		onGetMonthEventListener?.invoke(month)?.also { events ->
			val maxEventsInRow = ((mDayWidth - paddingEvents.horizontal) / (radiusEvents * 2 + paddingEvents.horizontal)).toInt()
			val date = month.clone() as Calendar
			date.set(Calendar.WEEK_OF_MONTH, 1)
			date.set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
			val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
			val eventsByDay = events.groupingBy { format.format(it.date.time) }.eachCount()
			val indexByDay = mutableMapOf<String, Int>()

			for (i in 0..events.lastIndex) {
				val key = format.format(events[i].date.time)
				val size = eventsByDay.getValue(key)
				if (size > maxEventsInRow) {
					continue
				}

				val index = indexByDay[key] ?: 0

				val halfSize = min(size, maxEventsInRow) / 2
				val dayOffset = abs(events[i].date.get(Calendar.DAY_OF_YEAR) - date.get(Calendar.DAY_OF_YEAR))
				val x = (dayOffset - (dayOffset / numberOfRows) * numberOfRows) * mDayWidth + mDayWidth / 2
				val y = dayOffset / numberOfRows * mDayHeight + mDayHeight / 2 + textSizeLabels + paddingLabels.vertical + mDayHeight / 4 + paddingEvents.top
				var horizontalOffset = (index - halfSize) * (radiusEvents * 2 + paddingEvents.horizontal)

				if (size % 2 == 0) {
					horizontalOffset += radiusEvents + paddingEvents.left
				}

				canvas.drawCircle(x + offset + horizontalOffset, y, radiusEvents.toFloat(), paintEvents.apply { color = events[i].color })

				indexByDay[key] = index + 1
			}
		}
	}
}










