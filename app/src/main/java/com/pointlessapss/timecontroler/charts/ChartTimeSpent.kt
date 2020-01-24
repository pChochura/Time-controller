package com.pointlessapss.timecontroler.charts

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.utils.Period
import com.pointlessapss.timecontroler.utils.Utils
import com.pointlessapss.timecontroler.views.ProgressWheel
import org.jetbrains.anko.find
import java.util.*

class ChartTimeSpent(context: Context, private val parent: Item?, private val tasks: List<Item>) :
	FrameLayout(context) {

	constructor(context: Context) : this(context, null, listOf())

	init {
		View.inflate(context, R.layout.chart_time_spent, this).post {
			val list = tasks.filter { Period.THIS_WEEK.meetsCondition(it.startDate!!) }
			val sum = list.sumByDouble { it.amount.toDouble() }.toFloat()
			val avg = sum / list.count()
			val now = Calendar.getInstance().apply {
				firstDayOfWeek = Calendar.MONDAY
				set(Calendar.HOUR_OF_DAY, 0)
				set(Calendar.MINUTE, 0)
			}
			val startOfWeek =
				(now.clone() as Calendar).apply { set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) }
			val potentialSum = Utils.getFieldWeekdaysCount(
				parent!!.weekdays,
				startOfWeek,
				now.apply { add(Calendar.DAY_OF_YEAR, -1) },
				null,
				Calendar.WEEK_OF_YEAR
			) * parent.amount

			find<ProgressWheel>(R.id.progressAverage).apply {
				setProgressColor(this@ChartTimeSpent.parent.color)
				setProgress(avg / this@ChartTimeSpent.parent.amount)
				setValue(this@ChartTimeSpent.parent.getTimeAmount(avg))
				invalidate()
			}

			find<ProgressWheel>(R.id.progressTotal).apply {
				setProgressColor(this@ChartTimeSpent.parent.color)
				setProgress(sum / potentialSum)
				setValue(this@ChartTimeSpent.parent.getTimeAmount(sum))
				invalidate()
			}
		}
	}
}