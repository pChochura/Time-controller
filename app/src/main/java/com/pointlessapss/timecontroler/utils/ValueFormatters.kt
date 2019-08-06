package com.pointlessapss.timecontroler.utils

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.ValueFormatter
import java.util.*

object ValueFormatters {

	val entryFormatter
		get() = object : ValueFormatter() {
			override fun getFormattedValue(value: Float): String {
				return String.format(
					Locale.getDefault(),
					"%dh %dm",
					value.toInt(),
					((value - value.toInt()) * 60).toInt()
				)
			}
		}

	val axisFormatter
		get() = object : ValueFormatter() {
			override fun getAxisLabel(value: Float, axis: AxisBase?) =
				String.format(Locale.getDefault(), "%dh", value.toInt())
		}
}