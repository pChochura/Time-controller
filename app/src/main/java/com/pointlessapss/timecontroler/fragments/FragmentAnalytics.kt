package com.pointlessapss.timecontroler.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.tabs.TabLayout
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.adapters.ListDayCountAdapter
import com.pointlessapss.timecontroler.adapters.ListDayCountMonthlyAdapter
import com.pointlessapss.timecontroler.adapters.ListPercentageAdapter
import com.pointlessapss.timecontroler.database.AppDatabase
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.models.MonthGroup
import com.pointlessapss.timecontroler.utils.DialogUtil
import com.pointlessapss.timecontroler.utils.Utils
import com.pointlessapss.timecontroler.utils.ValueFormatters
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.uiThread
import java.util.*

class FragmentAnalytics : FragmentBase() {

	private val months = Calendar.getInstance().let {
		(Calendar.JANUARY..Calendar.DECEMBER).map { month ->
			Utils.formatMonthLong.format(it.apply { set(Calendar.MONTH, month) }.time)
		}
	}

	private lateinit var db: AppDatabase
	private lateinit var tasksByTitle: Map<String, MutableList<Item>?>
	private lateinit var tasksByDayPercentage: Map<Item?, Double?>
	private lateinit var tasksDone: MutableList<Item>

	private lateinit var font: Typeface
	private var textColor: Int = 0
	private var textColor2: Int = 0

	override fun getLayoutId() = R.layout.fragment_analytics

	override fun created() {
		init()
		getTasks {
			setDayCountList()
			setPercentageList()
			setHoursChart()
		}
	}

	private fun getTasks(callback: () -> Unit) {
		doAsync {
			val year = Calendar.getInstance().get(Calendar.YEAR)
			val tasksCreated = db.itemDao().getAll()
			tasksDone = db.itemDao().getAll(true).filter { it.startDate?.get(Calendar.YEAR) == year }.toMutableList()
			tasksByTitle = tasksDone
				.groupingBy { it.title }
				.aggregate { _, acc: MutableList<Item>?, e, first ->
					if (first) {
						mutableListOf(e)
					} else {
						acc?.apply { add(e) }
					}
				}
			tasksByDayPercentage =
				tasksByTitle.filter { it.value?.find { item -> item.wholeDay } == null }.mapKeys { entry ->
					tasksCreated.find { it.title == entry.key && !it.done }
				}.mapValues { entry ->
					entry.value?.let { list ->
						list.sumByDouble { item ->
							item.amount.toDouble()
						} / (entry.key!!.amount * list.size)
					}
				}
			uiThread {
				callback.invoke()
			}
		}
	}

	private fun init() {
		font = ResourcesCompat.getFont(context!!, R.font.josefin_sans)!!
		textColor = ContextCompat.getColor(context!!, R.color.colorText1)
		textColor2 = ContextCompat.getColor(context!!, R.color.colorText2)
	}

	private fun setDayCountList() {
		rootView!!.find<RecyclerView>(R.id.listDayCount).apply {
			layoutManager = LinearLayoutManager(context!!, RecyclerView.HORIZONTAL, false)
			adapter = ListDayCountAdapter(tasksByTitle.toList()).apply {
				setOnClickListener { pos ->
					showDayCountInfo(tasksByTitle.toList()[pos])
				}
			}
		}
	}

	private fun setPercentageList() {
		rootView!!.find<RecyclerView>(R.id.listPercentage).apply {
			layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
			adapter = ListPercentageAdapter(tasksByDayPercentage.toList())
		}
	}

	private fun setHoursChart() {
		rootView!!.find<BarChart>(R.id.chartHours).apply {
			legend.isEnabled = false
			description.isEnabled = false
			isHighlightPerDragEnabled = false
			isHighlightPerTapEnabled = false
			setScaleEnabled(false)
			xAxis.apply {
				formatAxis()
				setDrawGridLines(false)
				position = XAxis.XAxisPosition.BOTTOM
				valueFormatter = IndexAxisValueFormatter(months)
			}
			axisRight.apply {
				formatAxis()
				axisMinimum = 0f
				valueFormatter = ValueFormatters.axisFormatter
			}
			axisLeft.apply {
				formatAxis()
				axisMinimum = 0f
				valueFormatter = ValueFormatters.axisFormatter
				setDrawLabels(false)
			}
		}


		tasksByTitle.filter { it.value?.find { item -> item.wholeDay } == null }.keys.forEach { title ->
			rootView!!.find<TabLayout>(R.id.tabsHours)
				.addTab(rootView!!.find<TabLayout>(R.id.tabsHours).newTab().apply {
					text = title
				})
		}

		rootView!!.find<TabLayout>(R.id.tabsHours).addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
			override fun onTabReselected(tab: TabLayout.Tab?) = Unit
			override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
			override fun onTabSelected(tab: TabLayout.Tab?) {
				showChart(tasksByTitle.toList()[tab!!.position])
			}
		})

		if (tasksByTitle.isNotEmpty()) {
			showChart(tasksByTitle.toList()[0])
		}
	}

	private fun AxisBase.formatAxis() {
		typeface = font
		textSize = 8f
		granularity = 1f
		isGranularityEnabled = true
		setCenterAxisLabels(false)
		textColor = this@FragmentAnalytics.textColor

	}

	private fun showChart(data: Pair<String, MutableList<Item>?>) {
		val values = data.second!!
			.groupingBy { MonthGroup(it) }
			.aggregate { _, acc: Float?, e, first ->
				if (first) {
					e.amount
				} else {
					acc?.plus(e.amount)
				}
			}.map {
				BarEntry(it.key.calendar.get(Calendar.MONTH).toFloat(), it.value!!)
			}

		rootView!!.find<BarChart>(R.id.chartHours).apply {
			this.data = BarData(BarDataSet(values, data.first).apply {
				valueTypeface = font
				valueTextSize = 10f
				color = data.second!!.first().color
				valueFormatter = ValueFormatters.entryFormatter
			})
			axisRight.removeAllLimitLines()
			axisRight.addLimitLine(
				LimitLine(
					data.second!!.fold(0f) { acc, item ->
						acc + item.amount
					} / values.size,
					context.getString(R.string.month_average)
				).apply {
					textSize = 10f
					typeface = font
					lineColor = Color.BLACK
				}
			)
		}.invalidate()
	}

	private fun showDayCountInfo(pair: Pair<String, MutableList<Item>?>) {
		DialogUtil.create(activity!!, R.layout.dialog_day_count_info, { dialog ->
			dialog.find<RecyclerView>(R.id.listDayCountMonthly).apply {
				layoutManager = LinearLayoutManager(context!!, RecyclerView.VERTICAL, false)
				adapter = ListDayCountMonthlyAdapter(pair.second!!)
			}

			dialog.find<AppCompatTextView>(R.id.textTitle).text = pair.first
			dialog.find<AppCompatTextView>(R.id.textDescription).text =
				resources.getString(R.string.item_count_monthly_description, pair.second!!.size)
		}, Utils.UNDEFINED_WINDOW_SIZE, ViewGroup.LayoutParams.WRAP_CONTENT)
	}

	fun setDb(db: AppDatabase) {
		this.db = db
	}
}