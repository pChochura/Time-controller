package com.pointlessapss.timecontroler.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.pointlessapss.timecontroler.R
import java.text.SimpleDateFormat
import java.util.*

class CalendarView(
	context: Context?,
	attrs: AttributeSet?,
	defStyleAttr: Int,
	defStyleRes: Int
) : View(context, attrs, defStyleAttr, defStyleRes) {

	private val numberOfDays = 7
	private val numberOfRows = 6
	private val formatLabel = SimpleDateFormat("EEE", Locale.getDefault())

	private var firstDayOfWeek = Calendar.MONDAY

	private lateinit var today: Calendar
	private var currentMonth = Calendar.JANUARY

	private var offset = 0

	private var mWidth = 0f
	private var mHeight = 0f
	private var mDayWidth = 0f
	private var mDayHeight = 0f

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

	constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

	constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0, 0)

	init {
		init(context, attrs)
	}

	private fun init(context: Context?, attrs: AttributeSet?) {
		val a = context?.theme?.obtainStyledAttributes(attrs, R.styleable.CalendarView, 0, 0)

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
				textTypeface = ResourcesCompat.getFont(context, a.getResourceId(R.styleable.CalendarView_cv_font_typeface, R.font.lato))!!
			} catch (ex: Exception) {
				Log.d(javaClass.name, ex.message!!)
			}

			a.recycle()
		}

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
		currentMonth = today.get(Calendar.MONTH)

		calculateSize()
	}

	private fun calculateSize() {
		post {
			mWidth = width.toFloat()
			mHeight = height.toFloat()

			mDayWidth = mWidth / numberOfDays
			mDayHeight = mHeight / numberOfRows
		}
	}

	override fun onDraw(canvas: Canvas) {
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

		val month = today.clone() as Calendar
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
		canvas.drawText(text, x + offset, paintLabels.textSize, paintLabels)
	}

	private fun drawDays(canvas: Canvas, month: Calendar, offset: Float) {
		val day = month.clone() as Calendar
		day.set(Calendar.DAY_OF_MONTH, 1)
		day.set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
		for (j in 0 until numberOfRows) {
			for (i in 0 until numberOfDays) {
				val text = day.get(Calendar.DAY_OF_MONTH).toString()
				val bounds = Rect()
				paintCurrentMonth.getTextBounds(text, 0, text.lastIndex, bounds)
				val currentOffset = (mDayWidth - bounds.width()) / 2 + offset
				canvas.drawText(text, i * mDayWidth + currentOffset, paintCurrentMonth.textSize + j * mDayHeight + 50, paintCurrentMonth)

				day.add(Calendar.DAY_OF_MONTH, 1)
			}
		}
	}
}