package com.pointlessapss.timecontroler.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.OverScroller
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.core.view.NestedScrollingChild
import androidx.core.view.ViewCompat
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.models.Event
import com.pointlessapss.timecontroler.models.Padding
import com.pointlessapss.timecontroler.utils.Utils
import com.pointlessapss.timecontroler.utils.dp
import java.util.*
import java.util.concurrent.TimeUnit
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
	private var onDaySelectedListener: ((selectedDay: Calendar) -> Unit)? = null

	private lateinit var scroller: OverScroller
	private lateinit var gestureDetector: GestureDetector

	private val eventsAll = mutableListOf<Event>()

	private val swipeMinDistance = 5
	private val swipeThresholdVelocity = 300
	private val scrollDuration = 300
	private val numberOfDays = 7
	private val numberOfRows = 6
	private val radiusEvents = 3.dp
	private val formatLabel = Utils.formatWeekdayShort

	private var firstDayOfWeek = Calendar.MONDAY

	private lateinit var today: Calendar
	private lateinit var currentMonth: Calendar
	private lateinit var selectedDay: Calendar

	private var offset = 0
	private var maxEventsInRow = 0

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

	fun addEvents(events: List<Event>) {
		eventsAll.addAll(events)
		post { refresh() }
	}

	fun addEvent(event: Event) {
		eventsAll.add(event)
		post { refresh() }
	}

	fun removeEventById(id: Int) {
		eventsAll.removeAll { it.id == id }
		post { refresh() }
	}

	fun setEventById(event: Event, id: Int = event.id) {
		val index = eventsAll.indexOfFirst { it.id == id }
		if (index != -1) {
			eventsAll[index] = event
			post { refresh() }
		}
	}

	fun scrollLeft() {
		val wantedOffset = -mWidth
		val diff = offset - wantedOffset
		scroller.startScroll(offset, 0, (-diff).toInt(), 0, scrollDuration)
		invalidate()
	}

	fun scrollRight() {
		val wantedOffset = mWidth
		val diff = offset - wantedOffset
		scroller.startScroll(offset, 0, (-diff).toInt(), 0, scrollDuration)
		invalidate()
	}

	fun setOnMonthChangeListener(onMonthChangeListener: (currentMonth: Calendar) -> Unit) {
		this.onMonthChangeListener = onMonthChangeListener
		post { this.onMonthChangeListener?.invoke(currentMonth) }
	}

	fun setOnDaySelectedListener(onDaySelectedListener: (selectedDay: Calendar) -> Unit) {
		this.onDaySelectedListener = onDaySelectedListener
		post { this.onDaySelectedListener?.invoke(selectedDay) }
	}

	fun getSelectedDay(): Calendar {
		return selectedDay
	}

	private fun refresh() {
		calculateEventPos()
		invalidate()
	}

	init {
		init(context, attrs)
	}

	private fun calculateEventPos() {
		val eventsByDay = eventsAll.groupingBy { Utils.formatDate.format(it.date.time) }.eachCount()
		val indexByDay = mutableMapOf<String, Int>()
		for (event in eventsAll) {
			val key = Utils.formatDate.format(event.date.time)
			val realSize = eventsByDay.getOrElse(key) { 0 }
			val size = min(realSize, maxEventsInRow)
			val index = indexByDay.getOrElse(key) { 0 }
			indexByDay[key] = index + 1

			if (realSize > maxEventsInRow && index >= maxEventsInRow - 1) {
				if (index == maxEventsInRow - 1) {
					event.color = 0
				} else {
					event.rect = null
					continue
				}
			}

			val month = (event.date.clone() as Calendar).apply {
				firstDayOfWeek = this@CalendarView.firstDayOfWeek
				set(Calendar.WEEK_OF_MONTH, 1)
				set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
			}

			val dayOffset = abs(TimeUnit.MILLISECONDS.toDays(event.date.timeInMillis - month.timeInMillis))
			val posY = dayOffset / numberOfDays
			val y = posY * mDayHeight + mDayHeight / 2
			val posX = dayOffset - posY * numberOfDays
			var x = posX * mDayWidth + mDayWidth / 2
			val verticalOffset = textSizeLabels + paddingLabels.vertical + mDayHeight / 4 + paddingEvents.top
			val horizontalOffset = (index - size / 2) * (radiusEvents * 2 + paddingEvents.horizontal)

			if (size % 2 == 0) {
				x += radiusEvents + paddingEvents.left
			}

			event.rect = Rect((x + horizontalOffset).toInt(), (y + verticalOffset).toInt(), 0, 0)
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

		onMonthChangeListener?.invoke(currentMonth)
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
		scroller = OverScroller(context, LinearOutSlowInInterpolator())
		gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
			override fun onDown(e: MotionEvent): Boolean {
				scroller.forceFinished(true)
				return true
			}

			override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
				offset -= distanceX.toInt()
				startNestedScroll(ViewCompat.SCROLL_AXIS_HORIZONTAL or ViewCompat.SCROLL_AXIS_VERTICAL)
				dispatchNestedScroll(distanceX.toInt(), distanceY.toInt(), 0, 0, null)
				invalidate()
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

				val monthDiff = currentMonth.get(Calendar.MONTH) - selectedDay.get(Calendar.MONTH)
				if (monthDiff != 0) {
					if (currentMonth.before(selectedDay)) {
						scrollLeft()
					} else {
						scrollRight()
					}
				}

				onDaySelectedListener?.invoke(selectedDay)

				invalidate()
				return true
			}
		})
	}

	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		super.onSizeChanged(w, h, oldw, oldh)
		if (w != oldw || h != oldh) {
			mWidth = w.toFloat()
			mHeight = h.toFloat()

			mDayWidth = mWidth / numberOfDays
			mDayHeight = (mHeight - textSizeLabels - paddingLabels.vertical) / numberOfRows

			maxEventsInRow =
				((mDayWidth - paddingEvents.horizontal) / (radiusEvents * 2 + paddingEvents.horizontal)).toInt()
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	override fun onTouchEvent(event: MotionEvent): Boolean {
		when {
			event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL -> {
				snap()
			}
			event.action == MotionEvent.ACTION_DOWN -> {
				if (!scroller.isFinished) {
					return false
				}
			}
		}

		return gestureDetector.onTouchEvent(event)
	}

	private fun snap() {
		val wantedOffset = round(offset / mWidth) * mWidth
		val diff = offset - wantedOffset
		scroller.startScroll(offset, 0, (-diff).toInt(), 0, scrollDuration)
		invalidate()
	}

	private fun updateOffset() {
		if (scroller.computeScrollOffset()) {
			offset = scroller.currX
			if (scroller.isFinished && offset != 0) {
				currentMonth.add(Calendar.MONTH, -offset.sign)
				offset = 0
				onMonthChangeListener?.invoke(currentMonth)
			}
			invalidate()
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
						paintToday.apply {
							color = if (currentMonth) {
								colorToday
							} else {
								ColorUtils.blendARGB(colorToday, Color.WHITE, 0.8f)
							}
						}
					)
					paint.color = colorTextToday
				} else {
					paintCurrentMonth.color = colorCurrentMonth
					paintOtherMonth.color = colorOtherMonth
				}

				canvas.drawText(text, x + horizontalOffset, y + verticalOffset, paint)

				day.add(Calendar.DAY_OF_MONTH, 1)
			}
		}
	}

	private fun drawEvents(canvas: Canvas, month: Calendar, offset: Float) {
		val currentMonth = month.get(Calendar.MONTH)
		month.set(Calendar.WEEK_OF_MONTH, 1)
		month.set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
		val eventOffset =
			if ((month.clone() as Calendar).apply {
					set(Calendar.MONTH, currentMonth + 1)
					set(Calendar.WEEK_OF_MONTH, 1)
					set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
				}.get(Calendar.DAY_OF_MONTH) == 1) 1
			else 2

		eventsAll.filter {
			isBetween(
				it.date,
				month,
				(month.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, numberOfDays * numberOfRows) })
		}.forEach { event ->
			event.rect?.also {
				val x = it.left
				var y = it.top

				val monthDiff = event.date.get(Calendar.MONTH) - currentMonth
				val diff = monthDiff.sign * (numberOfRows - eventOffset) * mDayHeight

				y += diff.toInt()

				if (event.color == 0) {
					canvas.drawLines(floatArrayOf(
						x + offset, (y - radiusEvents).toFloat(),
						x + offset, (y + radiusEvents).toFloat(),
						x + offset - radiusEvents, y.toFloat(),
						x + offset + radiusEvents, y.toFloat()
					), paintEvents.apply { color = colorCurrentMonth; strokeWidth = 1.dp.toFloat() })
				} else {
					canvas.drawCircle(
						x + offset,
						y.toFloat(),
						radiusEvents.toFloat(),
						paintEvents.apply {
							color = if (monthDiff == 0) {
								event.color
							} else {
								ColorUtils.blendARGB(event.color, Color.WHITE, 0.8f)
							}
						}
					)
				}
			}
		}
	}

	private fun isBetween(date: Calendar, start: Calendar, end: Calendar): Boolean {
		val realDate = (date.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
		return realDate.after(start) && realDate.before(end)
	}
}