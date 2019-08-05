package com.pointlessapss.timecontroler.views

import android.content.Context
import android.content.res.TypedArray
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
import com.pointlessapss.timecontroler.models.Padding
import com.pointlessapss.timecontroler.utils.dp

class ProgressWheel(
	context: Context,
	attrs: AttributeSet,
	defStyleAttr: Int,
	defStyleRes: Int
) : View(context, attrs, defStyleAttr, defStyleRes) {

	private var mWidth: Int = 0
	private var mHeight: Int = 0

	private var boundsLabel: Rect? = null

	private var padding: Padding = Padding(verticalPadding = 10.dp)

	private lateinit var paintLabel: TextPaint
	private lateinit var paintValue: TextPaint
	private lateinit var paintProgress: Paint
	private lateinit var paintRim: Paint

	@ColorInt private var colorTextLabel: Int = 0
	@ColorInt private var colorTextValue: Int = 0
	@ColorInt private var colorProgress: Int = 0
	@ColorInt private var colorRim: Int = 0

	private var textSizeLabel = 12f
	private var textSizeValue = 20f

	private var widthProgress = 30f
	private var progress = 0.5f

	private var label: String? = null
	private var value: String? = null

	private var textTypeface: Typeface? = null

	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

	constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0, 0)

	init {
		init(context, attrs)
	}

	private fun obtainStyles(a: TypedArray?, context: Context) {
		a?.also {
			try {
				colorTextLabel = a.getColor(
					R.styleable.ProgressWheel_pw_color_text_label,
					ContextCompat.getColor(context, R.color.colorText2)
				)
				colorTextValue = a.getColor(
					R.styleable.ProgressWheel_pw_color_text_value,
					ContextCompat.getColor(context, R.color.colorText1)
				)
				colorProgress = a.getColor(
					R.styleable.ProgressWheel_pw_color_progress,
					ContextCompat.getColor(context, R.color.colorAccent)
				)
				colorRim = a.getColor(
					R.styleable.ProgressWheel_pw_color_rim,
					ContextCompat.getColor(context, R.color.colorText2)
				)
				textSizeLabel = a.getDimension(
					R.styleable.ProgressWheel_pw_font_size_label,
					textSizeLabel
				)
				textSizeValue = a.getDimension(
					R.styleable.ProgressWheel_pw_font_size_value,
					textSizeValue
				)
				textTypeface = ResourcesCompat.getFont(
					context, a.getResourceId(
						R.styleable.ProgressWheel_pw_font_typeface,
						R.font.josefin_sans
					)
				)!!
				widthProgress = a.getDimension(
					R.styleable.ProgressWheel_pw_width_progress,
					widthProgress
				)
				progress = a.getFloat(
					R.styleable.ProgressWheel_pw_progress,
					progress
				)
				padding.top = a.getDimension(
					R.styleable.ProgressWheel_pw_padding_label_value,
					padding.top.toFloat()
				).toInt()
				padding.bottom = a.getDimension(
					R.styleable.ProgressWheel_pw_padding_label_value,
					padding.bottom.toFloat()
				).toInt()
				label = a.getString(R.styleable.ProgressWheel_pw_label)
				value = a.getString(R.styleable.ProgressWheel_pw_value)

			} catch (ex: Exception) {
				Log.d(javaClass.name, ex.message!!)
			}

			a.recycle()
		}
	}

	private fun init(context: Context, attrs: AttributeSet) {
		val a = context.theme.obtainStyledAttributes(attrs, R.styleable.ProgressWheel, 0, 0)

		obtainStyles(a, context)

		paintLabel = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
			color = colorTextLabel
			textSize = textSizeLabel
			if (textTypeface != null) {
				typeface = textTypeface
			}
		}
		paintValue = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
			color = colorTextValue
			textSize = textSizeValue
			if (textTypeface != null) {
				typeface = textTypeface
			}
		}
		paintProgress = Paint(Paint.ANTI_ALIAS_FLAG).apply {
			color = colorProgress
			style = Paint.Style.STROKE
			strokeCap = Paint.Cap.ROUND
			strokeWidth = widthProgress
		}
		paintRim = Paint(Paint.ANTI_ALIAS_FLAG).apply {
			color = colorRim
			style = Paint.Style.STROKE
			strokeCap = Paint.Cap.ROUND
			strokeWidth = widthProgress
		}

		calculateBounds()
	}

	private fun calculateBounds() {
		if (label == null) {
			return
		}
		val textWidthLabel = paintLabel.measureText(label)
		if (boundsLabel != null) {
			boundsLabel?.set(
				((mWidth - textWidthLabel) / 2).toInt(),
				(mHeight / 2 + textSizeLabel + padding.top).toInt(),
				0,
				0
			)
		} else {
			boundsLabel = Rect(
				((mWidth - textWidthLabel) / 2).toInt(),
				(mHeight / 2 + textSizeLabel + padding.top).toInt(),
				0,
				0
			)
		}
	}

	fun setProgress(progress: Float) {
		this.progress = progress
	}

	fun setValue(value: String) {
		this.value = value
	}

	fun setLabel(label: String) {
		this.label = label
		calculateBounds()
	}

	fun setProgressColor(color: Int) {
		this.colorProgress = color
		paintProgress.color = color
	}

	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		if (w != oldw || h != oldh) {
			mWidth = w
			mHeight = h

			calculateBounds()
		}
	}

	override fun draw(canvas: Canvas) {
		super.draw(canvas)

		drawRim(canvas)
		drawProgress(canvas)
		drawLabel(canvas)
		drawValue(canvas)
	}

	private fun drawRim(canvas: Canvas) {
		canvas.drawArc(
			widthProgress,
			widthProgress,
			mWidth.toFloat() - widthProgress,
			mHeight.toFloat() - widthProgress,
			0f,
			360f,
			false,
			paintRim
		)
	}

	private fun drawProgress(canvas: Canvas) {
		canvas.drawArc(
			widthProgress,
			widthProgress,
			mWidth.toFloat() - widthProgress,
			mHeight.toFloat() - widthProgress,
			-90f,
			progress * 360f,
			false,
			paintProgress
		)
	}

	private fun drawLabel(canvas: Canvas) {
		label?.also { l ->
			boundsLabel?.also { b ->
				canvas.drawText(l, b.left.toFloat(), b.top.toFloat(), paintLabel)
			}
		}
	}

	private fun drawValue(canvas: Canvas) {
		value?.also { v ->
			val textWidthValue = paintValue.measureText(v)
			canvas.drawText(
				v,
				(mWidth - textWidthValue) / 2,
				mHeight / 2f - padding.bottom,
				paintValue
			)
		}
	}
}