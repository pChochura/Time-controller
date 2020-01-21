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
			val list = tasks.filter { Period.THIS_WEEK.meetsCondition(it.startDate!!) }
			val sum = list.sumByDouble { it.amount.toDouble() }.toFloat()
			val avg = sum / list.count()

			find<ProgressWheel>(R.id.progressAverage).apply {
				setProgressColor(this@ChartTimeSpent.parent?.color!!)
				setProgress(avg / this@ChartTimeSpent.parent.amount)
				setValue(this@ChartTimeSpent.parent.getTimeAmount(avg))
				invalidate()
			}

			find<ProgressWheel>(R.id.progressTotal).apply {
				setProgressColor(this@ChartTimeSpent.parent?.color!!)
				setProgress(sum / (this@ChartTimeSpent.parent.amount * list.count()))
				setValue(this@ChartTimeSpent.parent.getTimeAmount(sum))
				invalidate()
			}
		}
	}
}