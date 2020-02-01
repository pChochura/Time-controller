package com.pointlessapss.timecontroler.charts

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.models.WeekGroup
import com.pointlessapss.timecontroler.utils.*
import kotlinx.android.synthetic.main.chart_day_count.view.*
import java.util.*

class ChartDayCount(context: Context, private val parent: Item?, private val tasks: List<Item>) :
	FrameLayout(context) {

	constructor(context: Context) : this(context, null, listOf())

	init {
		View.inflate(context, R.layout.chart_day_count, this).post {
			initChart()
			showChart()
		}
	}

	private fun initChart() {
		chartDayCount.apply {
			legend.isEnabled = false
			description.isEnabled = false
			isHighlightPerDragEnabled = false
			isHighlightPerTapEnabled = false
			setNoDataTextColor(ContextCompat.getColor(context, R.color.colorText3))
			setNoDataTextTypeface(ResourcesCompat.getFont(context, R.font.lato))
			setScaleEnabled(false)
			xAxis.apply {
				formatAxis()
				setDrawGridLines(false)
				setDrawAxisLine(false)
				position = XAxis.XAxisPosition.BOTTOM
				valueFormatter = ValueFormatters.formatterWeek
			}
			axisRight.apply {
				formatAxis()
				setDrawAxisLine(false)
				enableGridDashedLine(10f, 10f, 0f)
				axisMinimum = 0f
				valueFormatter = ValueFormatters.formatterNumber
			}
			axisLeft.apply {
				formatAxis()
				setDrawAxisLine(false)
				enableGridDashedLine(10f, 10f, 0f)
				axisMinimum = 0f
				valueFormatter = ValueFormatters.formatterNumber
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
		textColor = ContextCompat.getColor(context, R.color.colorText3)
	}

	private fun showChart() {
		val values = mutableListOf<BarEntry>()
		tasks.groupingBy { WeekGroup(it) }
			.aggregate { _, acc: MutableList<Item>?, e, first ->
				if (first) {
					mutableListOf(e)
				} else {
					acc?.apply {
						add(e)
					}
				}
			}.toSortedMap(kotlin.Comparator { v1, v2 -> v1.getIndex().compareTo(v2.getIndex()) })
			.map { entry ->
				val count =
					entry.value?.distinctBy { it.startDate?.get(Calendar.DAY_OF_WEEK) }?.count()?.toFloat()!!
				val max = Utils.getFieldWeekdaysCount(
					parent?.weekdays!!,
					(entry.key.calendar.clone() as Calendar).apply {
						set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
					},
					field = Calendar.WEEK_OF_YEAR
				).toFloat()
				values.apply {
					add(
						BarEntry(
							entry.key.getIndex(), max, true
						)
					)
					add(
						BarEntry(
							entry.key.getIndex(), count
						)
					)
				}
			}

		val now = Utils.date
		chartDayCount.apply {
			this.data = BarData(BarDataSet(values, this@ChartDayCount.parent?.title!!).apply {
				valueTypeface = ResourcesCompat.getFont(context, R.font.lato)
				valueTextSize = 10f
				valueFormatter = ValueFormatters.formatterNumber
				valueTextColor = ContextCompat.getColor(context, R.color.colorText3)
				colors = listOf(
					ColorUtils.setAlphaComponent(this@ChartDayCount.parent.color, 50),
					this@ChartDayCount.parent.color
				)
			})
			renderer =
				RoundedBarChartRenderer(this@apply, animator, viewPortHandler, 2.dp.toFloat())
			setVisibleXRange(1f, 6f)
			moveViewToX(now.get(Calendar.WEEK_OF_YEAR) + now.get(Calendar.YEAR) * 52f)
		}.invalidate()
	}
}