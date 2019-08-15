package com.pointlessapss.timecontroler.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.utils.dp
import org.jetbrains.anko.find
import java.text.SimpleDateFormat
import java.util.*

class MonthPickerView(
	context: Context,
	attrs: AttributeSet?,
	defStyleAttr: Int,
	defStyleRes: Int
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

	private val formatMonth = SimpleDateFormat("MMM", Locale.getDefault())
//	private val formatYear = SimpleDateFormat("yyyy", Locale.getDefault())

	val selectedDate: Calendar = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }

	private var size = 0
	private var topBarSize = 0

	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

	constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0, 0)

	init {
		post {
			val outValue = TypedValue()
			getContext().theme.resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, outValue, true)

			addView(LinearLayout(context).apply {
				layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
				gravity = Gravity.CENTER
				orientation = LinearLayout.HORIZONTAL
				id = TOP_BAR_ID
				setBackgroundColor(ContextCompat.getColor(context, R.color.colorAccent))
//				addView(AppCompatTextView(context).apply {
//					layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
//					text = formatYear.format(selectedDate.time)
//					isClickable = true
//					setBackgroundResource(outValue.resourceId)
//					setPadding(10.dp, 10.dp, 10.dp, 10.dp)
//					setTextColor(ContextCompat.getColor(context, R.color.colorText2))
//					setTextSize(TypedValue.COMPLEX_UNIT_SP, 40f)
//				})
				addView(AppCompatTextView(context).apply {
					id = CURRENT_MONTH_ID
					layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
					text = formatMonth.format(selectedDate.time)
					setPadding(10.dp, 10.dp, 10.dp, 10.dp)
					setTextColor(ContextCompat.getColor(context, R.color.colorText3))
					setTextSize(TypedValue.COMPLEX_UNIT_SP, 40f)
				})
			})

			find<View>(TOP_BAR_ID).post {
				topBarSize = find<View>(TOP_BAR_ID).height
				size = width / 4

				layoutParams.height = size * 3 + topBarSize

				val currentMonth = selectedDate.get(Calendar.MONTH)

				addView(AppCompatImageView(context).apply {
					id = INDICATOR_ID
					layoutParams = LayoutParams(size, size)
					setImageResource(R.drawable.ic_circle)
					setColorFilter(ContextCompat.getColor(context, R.color.colorAccent))
					x = ((currentMonth - (currentMonth / 4) * 4) * size).toFloat()
					y = (currentMonth / 4 * size).toFloat() + topBarSize
				})

				Calendar.getInstance().let { date ->
					(Calendar.JANUARY..Calendar.DECEMBER).forEach { month ->
						date.set(Calendar.MONTH, month)
						addView(AppCompatTextView(context).apply {
							id = MONTH_IDS[month]
							layoutParams = LayoutParams(size, size)
							text = formatMonth.format(date.time)
							gravity = Gravity.CENTER
							x = ((month - (month / 4) * 4) * size).toFloat()
							y = (month / 4 * size).toFloat() + topBarSize
							isClickable = true
							setOnClickListener {
								translateIndicator(month)
								refreshTitle()
							}
							setBackgroundResource(outValue.resourceId)
							setPadding(10.dp, 10.dp, 10.dp, 10.dp)
							setTextColor(
								ContextCompat.getColor(
									context,
									if (currentMonth == month)
										R.color.colorText3
									else R.color.colorText1
								)
							)
							setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
						})
					}
				}
			}
		}
	}

	private fun translateIndicator(month: Int) {
		find<View>(INDICATOR_ID).apply {
			x = ((month - (month / 4) * 4) * size).toFloat()
			y = (month / 4 * size).toFloat() + topBarSize
		}
		find<AppCompatTextView>(MONTH_IDS[selectedDate.get(Calendar.MONTH)]).apply {
			setTextColor(ContextCompat.getColor(context, R.color.colorText1))
		}
		find<AppCompatTextView>(MONTH_IDS[month]).apply {
			setTextColor(ContextCompat.getColor(context, R.color.colorText3))
		}
		selectedDate.set(Calendar.MONTH, month)
	}

	private fun refreshTitle() {
		find<AppCompatTextView>(CURRENT_MONTH_ID).text = formatMonth.format(selectedDate.time)
	}

	companion object {
		val TOP_BAR_ID = View.generateViewId()
		val INDICATOR_ID = View.generateViewId()
		val CURRENT_MONTH_ID = View.generateViewId()
		val MONTH_IDS = (Calendar.JANUARY..Calendar.DECEMBER).map { View.generateViewId() }.toIntArray()
	}
}