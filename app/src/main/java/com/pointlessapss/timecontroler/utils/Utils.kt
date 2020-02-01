package com.pointlessapss.timecontroler.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Point
import android.text.method.KeyListener
import android.widget.EditText
import androidx.annotation.FloatRange
import androidx.core.content.ContextCompat
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.models.Item
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

val Int.dp: Int
	get() = (this * Resources.getSystem().displayMetrics.density).toInt()

fun Double.round(decimals: Int = 2): Double {
	var multiplier = 1.0
	repeat(decimals) { multiplier *= 10 }
	return round(this * multiplier) / multiplier
}

object Utils {
	val formatDateWeekday
		get() = SimpleDateFormat("EEEE, dd.MM.yyyy", Locale.getDefault())

	val formatDate
		get() = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

	val formatTime
		get() = SimpleDateFormat("HH:mm", Locale.getDefault())

	val formatWeekdayShort
		get() = SimpleDateFormat("EEE", Locale.getDefault())

	val formatMonthLong
		get() = SimpleDateFormat("MMMM", Locale.getDefault())

	val date: Calendar
		get() = Calendar.getInstance().apply {
			firstDayOfWeek = Calendar.MONDAY
		}

	const val UNDEFINED_WINDOW_SIZE = Integer.MAX_VALUE

	@FloatRange(from = 0.0, to = 1.0)
	fun getLuminance(color: Int): Float {
		return (0.299f * Color.red(color) + 0.587f * Color.green(color) + 0.114f * Color.blue(color)) / 255f
	}

	fun getScreenSize() =
		Point(
			Resources.getSystem().displayMetrics.widthPixels,
			Resources.getSystem().displayMetrics.heightPixels
		)

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

		val date = date

		return (Calendar.SUNDAY..Calendar.SATURDAY)
			.filter { weekdays[it - 1] }
			.map { day ->
				formatWeekdayShort.format(date.apply {
					set(Calendar.DAY_OF_WEEK, day)
				}.time)
			}.joinToString { it }
	}

	fun createItemDescription(context: Context, item: Item): String {
		if (item.startDate == null || item.wholeDay) {
			return context.resources.getString(R.string.whole_day)
		}

		if (item.amount == 0f) {
			return formatTime.format(item.startDate!!.time)
		}

		val endTime = date
		endTime.timeInMillis = item.startDate!!.timeInMillis
		endTime.add(Calendar.HOUR_OF_DAY, item.amount.toInt())
		endTime.add(Calendar.MINUTE, ((item.amount - item.amount.toInt()) * 60).toInt())
		return "${formatTime.format(item.startDate!!.time)} - ${formatTime.format(endTime.time)}"
	}

	fun getFieldWeekdaysCount(
		weekdays: BooleanArray,
		start: Calendar,
		limit: Calendar? = null,
		disabledDays: List<Calendar>? = null,
		field: Int = Calendar.MONTH
	): Int {
		val current = start.get(field)
		var count = 0
		while (start.get(field) == current) {
			if (limit?.before(start) == true) {
				break
			}

			if (weekdays[start.get(Calendar.DAY_OF_WEEK) - 1] && disabledDays?.firstOrNull {
					it.get(Calendar.DAY_OF_YEAR) == start.get(Calendar.DAY_OF_YEAR)
							&& it.get(Calendar.YEAR) == start.get(Calendar.YEAR)
				} == null) {
				count++
			}
			start.add(Calendar.DAY_OF_MONTH, 1)
		}
		return count
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

	fun getInitials(text: String) =
		text.replace(Regex("(\\b\\w)\\w* ?"), "\$1").toUpperCase(Locale.getDefault())
}