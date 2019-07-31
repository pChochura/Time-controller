package com.pointlessapss.timecontroler.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
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
import androidx.core.view.NestedScrollingChild
import androidx.core.view.ViewCompat
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.utils.dp
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.sign

class CalendarView(
	context: Context,
	attrs: AttributeSet,
	defStyleAttr: Int,
	defStyleRes: Int
) : View(context, attrs, defStyleAttr, defStyleRes), NestedScrollingChild {

	private class Padding(var top: Int = 0, var bottom: Int = 0, var left: Int = 0, var right: Int = 0) {
		val horizontal: Int
			get() = left + right
		val vertical: Int
			get() = top + bottom

		constructor(padding: Int = 0) : this(padding, padding, padding, padding)
		constructor(horizontalPadding: Int = 0, verticalPadding: Int = 0) : this(
			verticalPadding,
			verticalPadding,
			horizontalPadding,
			horizontalPadding
		)
	}

	private var onMonthChangeListener: ((currentMonth: Calendar) -> Unit)? = null

	private lateinit var scroller: OverScroller
	private lateinit var gestureDetector: GestureDetector

	private var isTouching = false

	private val snappingThreshold = 2
	private val scrollDuration = 300
	private val numberOfDays = 7
	private val numberOfRows = 6
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

	private lateinit var paintBg: Paint
	private lateinit var paintSelectedDay: Paint
	private lateinit var paintToday: Paint
	private lateinit var paintLabels: TextPaint
	private lateinit var paintCurrentMonth: TextPaint
	private lateinit var paintOtherMonth: TextPaint

	@ColorInt private var colorBg: Int = 0
	@ColorInt private var colorSelectedDay: Int = 0
	@ColorInt private var colorToday: Int = 0
	@ColorInt private var colorLabels: Int = 0
	@ColorInt private var colorCurrentMonth: Int = 0
	@ColorInt private var colorOtherMonth: Int = 0

	private var textSizeLabels = 25f
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

	init {
		init(context, attrs)
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
			} catch (ex: Exception) {
				Log.d(javaClass.name, ex.message!!)
			}

			a.recycle()
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

		today = Calendar.getInstance()
		today.firstDayOfWeek = firstDayOfWeek
		currentMonth = today.clone() as Calendar
		selectedDay = currentMonth.clone() as Calendar

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
				if (velocityX < 0) {
					scroller.startScroll(offset, 0, -(offset + mWidth).toInt(), 0, scrollDuration)
				} else {
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

		calculateSize()
	}

	private fun calculateSize() {
		post {
			mWidth = width.toFloat()
			mHeight = height.toFloat()

			mDayWidth = mWidth / numberOfDays
			mDayHeight = (mHeight - textSizeLabels - paddingLabels.vertical) / numberOfRows
		}
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
		drawMonth(canvas, 0)
		drawMonth(canvas, -1)
		drawMonth(canvas, 1)
	}

	private fun drawBackground(canvas: Canvas) {
		canvas.drawRect(0f, 0f, mWidth, mHeight, paintBg)
	}

	private fun drawMonth(canvas: Canvas, monthOffset: Int) {
		drawLabels(canvas, offset + monthOffset * mWidth)

		val month = currentMonth.clone() as Calendar
		month.add(Calendar.MONTH, monthOffset)
		drawDays(canvas, month, offset + monthOffset * mWidth)
	}

	private fun drawLabels(canvas: Canvas, offset: Float) {
		val day = today.clone() as Calendar
		day.set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
		for (i in 0 until numberOfDays) {
			val x = i * mDayWidth + offset
			drawLabel(canvas, day, x)

			day.add(Calendar.DAY_OF_MONTH, 1)
		}
	}

	private fun drawLabel(canvas: Canvas, day: Calendar, x: Float) {
		val text = formatLabel.format(day.time)
		val bounds = Rect()
		paintLabels.getTextBounds(text, 0, text.lastIndex, bounds)
		val offset = (mDayWidth - bounds.width()) / 2
		canvas.drawText(text, x + offset, paintLabels.textSize + paddingLabels.top, paintLabels)
	}

	private fun drawDays(canvas: Canvas, month: Calendar, offset: Float) {
		val day = month.clone() as Calendar
		day.set(Calendar.WEEK_OF_MONTH, 1)
		day.set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
		for (j in 0 until numberOfRows) {
			for (i in 0 until numberOfDays) {
				val currentMonth = day.get(Calendar.MONTH) == currentMonth.get(Calendar.MONTH)
				val selectedDay =
					day.get(Calendar.YEAR) == selectedDay.get(Calendar.YEAR) &&
							day.get(Calendar.DAY_OF_YEAR) == selectedDay.get(Calendar.DAY_OF_YEAR)
				val text = day.get(Calendar.DAY_OF_MONTH).toString()
				val bounds = Rect()
				val paint = if (currentMonth) paintCurrentMonth else paintOtherMonth
				paint.getTextBounds(text, 0, text.lastIndex, bounds)
				val verticalOffset = (mDayHeight + paint.textSize) / 2
				val horizontalOffset = (mDayWidth - paint.measureText(text)) / 2
				val x = i * mDayWidth + offset
				val y = j * mDayHeight + paddingLabels.vertical + textSizeLabels

				if (selectedDay) {
					canvas.drawRect(x, y, x + mDayWidth, y + mDayHeight, paintSelectedDay)
					canvas.drawLine(x, y + mDayHeight / 2, x + mDayWidth, y + mDayHeight / 2, paintCurrentMonth)
					canvas.drawLine(x + mDayWidth / 2, y, x + mDayWidth / 2, y + mDayHeight, paintCurrentMonth)
				}

//				val layout = StaticLayout(text, paint, mDayWidth.toInt(), Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false)

//				canvas.save()
//				canvas.translate(x, y + verticalOffset)
//				layout.draw(canvas)
//				canvas.restore()
				canvas.drawText(text, x + horizontalOffset, y + verticalOffset, paint)

				day.add(Calendar.DAY_OF_MONTH, 1)
			}
		}
	}
}