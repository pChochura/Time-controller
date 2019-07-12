package com.pointlessapss.timecontroler.utils

import android.graphics.Color
import androidx.annotation.FloatRange
import java.text.SimpleDateFormat
import java.util.*

object Utils {
	val dateFormat
			get() = SimpleDateFormat("HH:mm, dd MMMM", Locale.getDefault())

	@FloatRange(from = 0.0, to = 1.0)
	fun getLuminance(color: Int): Float {
		return (0.299f * Color.red(color) + 0.587f * Color.green(color) + 0.114f * Color.blue(color)) / 255f
	}
}