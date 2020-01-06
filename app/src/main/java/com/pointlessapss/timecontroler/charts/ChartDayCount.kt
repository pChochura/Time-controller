package com.pointlessapss.timecontroler.charts

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.utils.Period
import com.pointlessapss.timecontroler.utils.Utils
import com.pointlessapss.timecontroler.views.ProgressLine
import org.jetbrains.anko.find
import java.util.*

class ChartDayCount(context: Context, private val parent: Item?, private val tasks: List<Item>) :
	FrameLayout(context) {

	constructor(context: Context) : this(context, null, listOf())

	init {
		View.inflate(context, R.layout.chart_day_count, this).post {
			val startDate = Calendar.getInstance().apply {
				firstDayOfWeek = Calendar.MONDAY
				set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
				set(Calendar.HOUR_OF_DAY, 0)
				set(Calendar.MINUTE, 0)
				set(Calendar.SECOND, 0)
			}
			val endDate = Calendar.getInstance().apply {
				firstDayOfWeek = Calendar.MONDAY
				set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
				set(Calendar.HOUR_OF_DAY, 23)
				set(Calendar.MINUTE, 59)
				set(Calendar.SECOND, 59)
			}

			for (i in 0..4) {
				val count = tasks.filter { Period.isBetween(it, startDate, endDate) }.distinctBy {
					it.startDate?.get(Calendar.YEAR)!! * 366 + it.startDate?.get(Calendar.DAY_OF_YEAR)!!
				}.count()
				val max = Utils.getFieldWeekdaysCount(
					parent?.weekdays!!,
					startDate.clone() as Calendar,
					field = Calendar.WEEK_OF_YEAR
				)

				context.resources.getIdentifier("bar${5 - i}", "id", context.packageName)
					.also { id ->
						find<ProgressLine>(id).apply {
							setProgressColor(this@ChartDayCount.parent.color)
							setProgress(count.toFloat() / max.toFloat())
							invalidate()
						}
					}
				context.resources.getIdentifier("summary${5 - i}", "id", context.packageName)
					.also { id ->
						find<AppCompatTextView>(id).apply {
							text = String.format("%d/%d", count, max)
						}
					}

				startDate.add(Calendar.WEEK_OF_YEAR, -1)
				endDate.add(Calendar.WEEK_OF_YEAR, -1)
			}

			find<View>(R.id.indicatorActualDayCount).backgroundTintList =
				ColorStateList.valueOf(parent?.color!!)
		}
	}
}