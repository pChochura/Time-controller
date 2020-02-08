package com.pointlessapss.timecontroler.charts

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.core.text.HtmlCompat
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.utils.Utils
import kotlinx.android.synthetic.main.chart_streak.view.*
import java.util.*
import java.util.concurrent.TimeUnit

class ChartStreak(context: Context, private val parent: Item?, private val tasks: List<Item>) :
	FrameLayout(context) {

	constructor(context: Context) : this(context, null, listOf())

	init {
		View.inflate(context, R.layout.chart_streak, this).post {
			val list = tasks.sortedByDescending { it.startDate }
				.distinctBy { TimeUnit.MILLISECONDS.toDays(it.startDate?.timeInMillis!!) }
			var current = 0
			var currentStreak: Int? = null
			var longestStreak = 0
			var date: Calendar? = Utils.date
			list.forEach {
				if (Utils.getDayDifference(it.startDate!!, date!!) > 1) {
					if (currentStreak == null) {
						currentStreak = current
					}
					if (current > longestStreak) {
						longestStreak = current
					}
					current = 0
				}
				current++
				date = it.startDate
			}

			if (current > longestStreak) {
				longestStreak = current
			}

			progressCurrentStreak.apply {
				setProgressColor(this@ChartStreak.parent?.color!!)
				setProgress((currentStreak?.toFloat() ?: 0.0f) / longestStreak)
				setValue(
					context.resources.getQuantityString(
						R.plurals.day,
						currentStreak ?: 0,
						currentStreak ?: 0
					)
				)
				invalidate()
			}

			textLongestStreak.text = HtmlCompat.fromHtml(
				context.resources.getQuantityString(
					R.plurals.longest_streak,
					longestStreak,
					longestStreak
				),
				HtmlCompat.FROM_HTML_MODE_COMPACT
			)
		}
	}
}