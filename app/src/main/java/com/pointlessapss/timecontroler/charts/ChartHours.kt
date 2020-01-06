package com.pointlessapss.timecontroler.charts

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.models.MonthGroup
import com.pointlessapss.timecontroler.utils.ValueFormatters
import org.jetbrains.anko.find

class ChartHours(context: Context, private val parent: Item?, private val tasks: List<Item>) :
	FrameLayout(context) {

	constructor(context: Context) : this(context, null, listOf())

	init {
		View.inflate(context, R.layout.chart_hours, this).post {
			initChart()
			showChart()
		}
	}

	private fun initChart() {
		rootView!!.find<BarChart>(R.id.chartHours).apply {
			legend.isEnabled = false
			description.isEnabled = false
			isHighlightPerDragEnabled = false
			isHighlightPerTapEnabled = false
			setNoDataTextColor(ContextCompat.getColor(context, R.color.colorText1))
			setNoDataTextTypeface(ResourcesCompat.getFont(context, R.font.lato))
			setScaleEnabled(false)
			xAxis.apply {
				formatAxis()
				setDrawGridLines(false)
				position = XAxis.XAxisPosition.BOTTOM
				valueFormatter = ValueFormatters.formatterMonth
			}
			axisRight.apply {
				formatAxis()
				axisMinimum = 0f
				valueFormatter = ValueFormatters.formatterHour
			}
			axisLeft.apply {
				formatAxis()
				axisMinimum = 0f
				valueFormatter = ValueFormatters.formatterHour
				setDrawLabels(false)
			}
		}
	}

	private fun AxisBase.formatAxis() {
		typeface = ResourcesCompat.getFont(context, R.font.lato)
		textSize = 8f
		granularity = 1f
		isGranularityEnabled = true
		setCenterAxisLabels(false)
		textColor = ContextCompat.getColor(context, R.color.colorText1)
	}

	private fun showChart() {
		val values = tasks
			.groupingBy { MonthGroup(it) }
			.aggregate { _, acc: Float?, e, first ->
				if (first) {
					e.amount
				} else {
					acc?.plus(e.amount)
				}
			}.map {
				BarEntry(
					it.key.month + it.key.year * 12f,
					it.value!!
				)
			}

		val average = tasks.fold(0f) { acc, item -> acc + item.amount } / values.size
		rootView!!.find<BarChart>(R.id.chartHours).apply {
			this.data = BarData(BarDataSet(values, this@ChartHours.parent?.title).apply {
				valueTypeface = ResourcesCompat.getFont(context, R.font.lato)
				valueTextSize = 10f
				color = this@ChartHours.parent?.color!!
				valueFormatter = ValueFormatters.formatterEntryHour
			})
			setVisibleXRange(1f, 6f)
			axisRight.removeAllLimitLines()
			axisRight.addLimitLine(
				LimitLine(
					average,
					context.getString(R.string.month_average, average.toInt())
				).apply {
					textSize = 10f
					typeface = ResourcesCompat.getFont(context, R.font.lato)
					lineColor = Color.BLACK
				}
			)
		}.invalidate()
	}
}
