package com.pointlessapss.timecontroler.utils

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.*
import kotlin.math.floor

object ValueFormatters {

	val formatterNumber
		get() = object : ValueFormatter() {
			override fun getAxisLabel(value: Float, axis: AxisBase?) =
				String.format(Locale.getDefault(), "%d", value.toInt())

			override fun getFormattedValue(value: Float) =
				String.format(Locale.getDefault(), "%d", value.toInt())
		}

	val formatterHour
		get() = object : ValueFormatter() {
			override fun getAxisLabel(value: Float, axis: AxisBase?) =
				String.format(Locale.getDefault(), "%dh", value.toInt())

			override fun getFormattedValue(value: Float): String {
				return String.format(
					Locale.getDefault(),
					"%dh %dm",
					value.toInt(),
					((value - value.toInt()) * 60).toInt()
				)
			}
		}

	val formatterMonth
		get() = object : ValueFormatter() {
			override fun getAxisLabel(value: Float, axis: AxisBase?): String {
				val year = floor(value / 12f).toInt()
				val month = value.toInt() - year * 12
				return Utils.formatMonthLong.format(GregorianCalendar(year, month, 1).time)
			}
		}

	val formatterWeek
		get() = object : ValueFormatter() {
			override fun getAxisLabel(value: Float, axis: AxisBase?): String {
				val year = floor(value / 52f).toInt()
				val week = value.toInt() - year * 52f
				return week.toString()
			}
		}
}