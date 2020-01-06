package com.pointlessapss.timecontroler.charts

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.utils.Period
import com.pointlessapss.timecontroler.views.ProgressWheel
import org.jetbrains.anko.find
import kotlin.math.floor

class ChartTimeSpent(context: Context, private val parent: Item?, private val tasks: List<Item>) :
	FrameLayout(context) {

	constructor(context: Context) : this(context, null, listOf())

	init {
		View.inflate(context, R.layout.chart_time_spent, this).post {
			val average = tasks.filter { Period.THIS_WEEK.meetsCondition(it.startDate!!) }.let { list ->
				list.sumByDouble { it.amount.toDouble() }.toFloat() / list.count()
			}

			find<ProgressWheel>(R.id.progress).apply {
				setProgressColor(this@ChartTimeSpent.parent?.color!!)
				setProgress(average / this@ChartTimeSpent.parent.amount)
				setValue(this@ChartTimeSpent.parent.getTimeAmount(average))
				invalidate()
			}
			find<View>(R.id.indicatorAverageTime).backgroundTintList = ColorStateList.valueOf(parent?.color!!)
			find<AppCompatTextView>(R.id.textAverageTime).text = context.getString(
				R.string.average_time,
				(average / this@ChartTimeSpent.parent.amount * 100).toInt()
			)
			find<AppCompatTextView>(R.id.textAssumedTime).text = context.getString(
				R.string.assumed_time,
				floor(parent.amount).toInt(),
				(parent.amount - floor(parent.amount)).toInt()
			)
		}
	}
}