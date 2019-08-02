package com.pointlessapss.timecontroler.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Point
import android.text.method.KeyListener
import android.util.DisplayMetrics
import android.widget.EditText
import androidx.annotation.FloatRange
import androidx.core.content.ContextCompat
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.models.Item
import java.text.SimpleDateFormat
import java.util.*

val Int.dp: Int
	get() = (this * Resources.getSystem().displayMetrics.density).toInt()

object Utils {
	val formatDate
		get() = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

	val formatTime
		get() = SimpleDateFormat("HH:mm", Locale.getDefault())

	val formatWeekdayShort
		get() = SimpleDateFormat("EEE", Locale.getDefault())

	val formatMonthLong
		get() = SimpleDateFormat("MMMM", Locale.getDefault())

	const val UNDEFINED_WINDOW_SIZE = Integer.MAX_VALUE

	@FloatRange(from = 0.0, to = 1.0)
	fun getLuminance(color: Int): Float {
		return (0.299f * Color.red(color) + 0.587f * Color.green(color) + 0.114f * Color.blue(color)) / 255f
	}

	fun getScreenSize(activity: Activity): Point {
		val displayMetrics = DisplayMetrics()
		activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
		return Point(displayMetrics.widthPixels, displayMetrics.heightPixels)
	}

	fun toggleEditText(editText: EditText, enabled: Boolean = false) {
		if (enabled) {
			editText.keyListener = (editText.tag ?: return) as KeyListener
		} else {
			editText.tag = editText.keyListener
			editText.keyListener = null
		}
	}

	fun joinWeekdaysToString(context: Context, weekdays: BooleanArray): String {
		if (!weekdays.contains(true)) {
			return context.resources.getString(R.string.weekdays)
		}

		val day = Calendar.getInstance()
		day.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

		val str = StringBuilder()

		weekdays.forEach {
			if (it) {
				str.append(", ")
					.append(formatWeekdayShort.format(day.time))
			}
			day.add(Calendar.DAY_OF_MONTH, 1)
		}

		return str.substring(2)
	}

	fun createItemDescription(context: Context, item: Item): String {
		if (item.startDate == null) {
			return context.resources.getString(R.string.whole_day)
		}

		if (item.amount == 0f) {
			return formatTime.format(item.startDate!!.time)
		}

		val endTime = Calendar.getInstance()
		endTime.timeInMillis = item.startDate!!.timeInMillis
		endTime.add(Calendar.HOUR_OF_DAY, item.amount.toInt())
		endTime.add(Calendar.MINUTE, ((item.amount - item.amount.toInt()) * 60).toInt())
		return "${formatTime.format(item.startDate!!.time)} - ${formatTime.format(endTime.time)}"
	}

	fun getColors(context: Context): IntArray {
		val numberOfColors = 16
		val output = IntArray(numberOfColors)
		return output.apply {
			this.forEachIndexed { i, _ ->
				val id = context.resources.getIdentifier("color$i", "color", context.packageName)
				output[i] = ContextCompat.getColor(context, id)
			}
		}
	}
}