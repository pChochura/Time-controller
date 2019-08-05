package com.pointlessapss.timecontroler.fragments

import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CandleDataSet
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.google.android.material.tabs.TabLayout
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.adapters.ListMonthProgressAdapter
import com.pointlessapss.timecontroler.database.AppDatabase
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.models.MonthGroup
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.uiThread
import java.util.*

class FragmentAnalytics : Fragment() {

	private var rootView: ViewGroup? = null

	private lateinit var db: AppDatabase
	private lateinit var tasksByMonth: Map<MonthGroup, MutableList<Item>?>

	private lateinit var listDayCount: RecyclerView

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_analytics, container, false) as ViewGroup

			init()
			getTasks {
				setMonthProgressList()
				setHoursChart()
			}
		}
		return rootView
	}

	private fun getTasks(callback: () -> Unit) {
		doAsync {
			tasksByMonth =
				db.itemDao().getAll(true)
					.filter { it.startDate?.get(Calendar.MONTH) == Calendar.JULY }
					.groupingBy {
						MonthGroup(
							it.startDate!!.get(Calendar.MONTH),
							it.startDate!!.get(Calendar.YEAR),
							it.title
						)
					}
					.aggregate { _, acc: MutableList<Item>?, e, first ->
						if (first) {
							mutableListOf(e)
						} else acc?.apply { add(e) }
					}
			uiThread {
				callback.invoke()
			}
		}
	}

	private fun init() {
		listDayCount = rootView!!.find(R.id.listMonthProgress)
	}

	private fun setMonthProgressList() {
		listDayCount.layoutManager = LinearLayoutManager(context!!, RecyclerView.HORIZONTAL, false)
		listDayCount.adapter = ListMonthProgressAdapter(tasksByMonth.toList())
	}

	private fun setHoursChart() {
		val font = ResourcesCompat.getFont(context!!, R.font.josefin_sans)
		val textColor = ContextCompat.getColor(context!!, R.color.colorText1)
		val chartHours = rootView!!.find<CandleStickChart>(R.id.chartHours)
		val tabsHours = rootView!!.find<TabLayout>(R.id.tabsHours)

		chartHours.apply {
			description = Description().apply { text = "" }
			axisLeft.setDrawLabels(false)
			setPinchZoom(false)
			isDoubleTapToZoomEnabled = false
			xAxis.apply {
				textSize = 5f
				position = XAxis.XAxisPosition.BOTTOM
				granularity = 1f
				isGranularityEnabled = true
				setAvoidFirstLastClipping(true)
				valueFormatter = object : ValueFormatter() {
					override fun getAxisLabel(value: Float, axis: AxisBase?): String {
						return String.format(Locale.getDefault(), "%02.0f", value)
					}
				}
				typeface = font
				setDrawGridLines(false)
				setLabelCount(31, true)
			}
			axisRight.apply {
				textSize = 5f
				granularity = 1f
				isGranularityEnabled = true
				valueFormatter = object : ValueFormatter() {
					override fun getAxisLabel(value: Float, axis: AxisBase?): String {
						return String.format(Locale.getDefault(), "%02.0f:00", value)
					}
				}
				typeface = font
			}
			legend.isEnabled = false

			isHighlightPerDragEnabled = false
		}

		val values = mutableListOf<CandleEntry>()
		tasksByMonth.values.forEachIndexed { i, list ->
			if (list?.first()?.wholeDay == false) {
				list.forEachIndexed { index, item ->
					val min = item.startDate!!.get(Calendar.HOUR_OF_DAY).toFloat()
					val max = min + item.amount
					values.add(
						CandleEntry(
							index.toFloat(),
							max,
							min,
							max,
							min
						)
					)
				}

				tabsHours.addTab(tabsHours.newTab().apply {
					text = list.first().title
					if (i == 0) {
						select()
					}
				})
			}
		}

		val set = CandleDataSet(values, "").apply {
			valueTypeface = font
			colors = tasksByMonth.map { it.value?.first()?.color }
			shadowColor = Color.DKGRAY
			shadowWidth = 0.7f
			increasingPaintStyle = Paint.Style.FILL
			decreasingPaintStyle = Paint.Style.FILL
			neutralColor = Color.BLUE
			increasingColor = Color.GREEN
			decreasingColor = Color.RED
			setDrawValues(true)
			valueFormatter = object : ValueFormatter() {
				override fun getCandleLabel(candleEntry: CandleEntry?): String {
					return String.format(Locale.getDefault(), "%.0f", candleEntry?.bodyRange)
				}
			}
		}

//		cds.setColor(Color.rgb(80, 80, 80));
//		cds.setShadowColor(Color.DKGRAY);
//		cds.setShadowWidth(0.7f);
//		cds.setDecreasingColor(Color.RED);
//		cds.setDecreasingPaintStyle(Paint.Style.FILL);
//		cds.setIncreasingColor(Color.rgb(122, 242, 84));
//		cds.setIncreasingPaintStyle(Paint.Style.STROKE);
//		cds.setNeutralColor(Color.BLUE);
//		cds.setValueTextColor(Color.RED);

		chartHours.apply {
			data = CandleData(set)
		}.invalidate()
	}

	fun setDb(db: AppDatabase) {
		this.db = db
	}
}
