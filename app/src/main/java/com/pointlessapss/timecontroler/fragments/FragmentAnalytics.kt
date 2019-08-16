package com.pointlessapss.timecontroler.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
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
import com.pointlessapss.timecontroler.adapters.*
import com.pointlessapss.timecontroler.database.AppDatabase
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.models.MonthGroup
import com.pointlessapss.timecontroler.models.Prize
import com.pointlessapss.timecontroler.utils.DialogUtil.Companion.create
import com.pointlessapss.timecontroler.utils.Utils
import com.pointlessapss.timecontroler.utils.ValueFormatters
import com.pointlessapss.timecontroler.views.MonthPickerView
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
	private lateinit var tasksByParent: Map<Int?, MutableList<Item>?>
	private lateinit var tasksByDayPercentage: Map<Item?, List<Float>?>
	private lateinit var tasksDone: MutableList<Item>
	private lateinit var tasksCreated: List<Item>

	private lateinit var listDayCountAdapter: ListDayCountAdapter
	private lateinit var listDayCountMonthlyAdapter: ListDayCountMonthlyAdapter
	private lateinit var listPercentageAdapter: ListPercentageAdapter
	private lateinit var listPrizeAdapter: ListPrizeAdapter
	private lateinit var listPrizePeriodicallyAdapter: ListPrizePeriodicallyAdapter

	private lateinit var font: Typeface
	private var textColor: Int = 0
	private var textColor2: Int = 0

	override fun getLayoutId() = R.layout.fragment_analytics

	override fun created() {
		init()
		getTasks {
			setPrizeList()
			setDayCountList()
			setPercentageList()
			setHoursChart()
		}
	}

	private fun getTasks(callback: () -> Unit) {
		doAsync {
			val year = Calendar.getInstance().get(Calendar.YEAR)
			tasksCreated = db.itemDao().getAll()
			tasksDone = db.itemDao().getAll(true).filter { it.startDate?.get(Calendar.YEAR) == year }.toMutableList()
			tasksByParent = tasksDone
				.groupingBy { it.parentId }
				.aggregate { _, acc: MutableList<Item>?, e, first ->
					if (first) {
						mutableListOf(e)
					} else {
						acc?.apply { add(e) }
					}
				}
			tasksByDayPercentage =
				tasksByParent.filter { it.value?.find { item -> item.wholeDay } == null }.mapKeys { entry ->
					tasksCreated.find { it.id == entry.key && !it.done }
				}.mapValues { entry ->
					entry.value?.let { list ->
						list.map { it.amount }
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

	private fun setPrizeList() {
		rootView!!.find<RecyclerView>(R.id.listPrize).apply {
			layoutManager = LinearLayoutManager(context!!, RecyclerView.HORIZONTAL, false)
			listPrizeAdapter =
				ListPrizeAdapter(tasksByParent.map { entry -> tasksCreated.find { it.id == entry.key }!! to entry.value }.filter { it.first.prize != null }).apply {
					setOnClickListener { pos ->
						showPrizeInfo(items[pos])
					}
				}
			adapter = listPrizeAdapter
		}
	}

	private fun setDayCountList() {
		rootView!!.find<RecyclerView>(R.id.listDayCount).apply {
			layoutManager = LinearLayoutManager(context!!, RecyclerView.HORIZONTAL, false)
			listDayCountAdapter =
				ListDayCountAdapter(tasksByParent.map { entry -> tasksCreated.find { it.id == entry.key }!! to entry.value }).apply {
					setOnClickListener { pos ->
						showDayCountInfo(items[pos])
					}
				}
			adapter = listDayCountAdapter
		}
	}

	private fun setPercentageList() {
		rootView!!.find<RecyclerView>(R.id.listPercentage).apply {
			layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
			listPercentageAdapter = ListPercentageAdapter(tasksByDayPercentage.toList()).apply {
				setOnClickListener { pos ->
					showPercentageInfo(items[pos])
				}
			}
			adapter = listPercentageAdapter
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

		tasksByParent.map { entry -> tasksCreated.find { it.id == entry.key }!! }
			.filter { !it.wholeDay }
			.forEach { parent ->
				rootView!!.find<TabLayout>(R.id.tabsHours)
					.addTab(rootView!!.find<TabLayout>(R.id.tabsHours).newTab().apply {
						text = parent.title
					})
			}

		rootView!!.find<TabLayout>(R.id.tabsHours).addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
			override fun onTabReselected(tab: TabLayout.Tab?) = Unit
			override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
			override fun onTabSelected(tab: TabLayout.Tab?) {
				showChart(tasksByParent.toList()[tab!!.position])
			}
		})

		if (tasksByParent.isNotEmpty()) {
			showChart(tasksByParent.toList()[0])
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

	private fun showChart(data: Pair<Int?, MutableList<Item>?>) {
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
			this.data = BarData(BarDataSet(values, tasksCreated.find { it.id == data.first }?.title).apply {
				valueTypeface = font
				valueTextSize = 10f
				color = tasksCreated.find { it.id == data.first }!!.color
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

	private fun showPrizeInfo(pair: Pair<Item, MutableList<Item>?>) {
		create(activity!!, R.layout.dialog_prize_info, { dialog ->
			dialog.find<RecyclerView>(R.id.listPrizePeriodically).apply {
				layoutManager = LinearLayoutManager(context!!, RecyclerView.VERTICAL, false)
				listPrizePeriodicallyAdapter = ListPrizePeriodicallyAdapter(pair).apply {
					setOnClickListener {
						showPeriodPickerDialog(pair.first.prize!!.type) {
							if (pair.first.settlements == null) {
								pair.first.settlements = mutableListOf(it)
							} else {
								pair.first.settlements?.add(it)
							}
							doAsync {
								db.itemDao().insertAll(pair.first)
							}
							notifyDataset()
							listPrizeAdapter.notifyDataset()
							dialog.find<AppCompatTextView>(R.id.textDescription).text =
								resources.let { res ->
									res.getString(
										R.string.item_description,
										res.getString(R.string.since_last_settlement),
										Prize.getPrizeSumSinceLast(pair.first, pair.second).toString()
									)
								}
						}
					}
				}
				adapter = listPrizePeriodicallyAdapter
			}

			dialog.find<AppCompatTextView>(R.id.textTitle).text = pair.first.title
			dialog.find<AppCompatTextView>(R.id.textDescription).text =
				resources.let { res ->
					res.getString(
						R.string.item_description,
						res.getString(R.string.since_last_settlement),
						Prize.getPrizeSumSinceLast(pair.first, pair.second).toString()
					)
				}
		}, Utils.UNDEFINED_WINDOW_SIZE, ViewGroup.LayoutParams.WRAP_CONTENT)
	}

	private fun showPeriodPickerDialog(prizeType: Prize.Type, callbackOk: (Calendar) -> Unit) {
		when (prizeType) {
			Prize.Type.PER_MONTH -> {
				create(activity!!, R.layout.dialog_picker_month, { dialog ->
					dialog.find<View>(R.id.buttonOk).setOnClickListener {
						callbackOk.invoke(dialog.find<MonthPickerView>(R.id.monthPicker).selectedDate)
						dialog.dismiss()
					}
				}, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
			}
			else -> {
				create(activity!!, R.layout.dialog_picker_date, { dialog ->
					val picker = dialog.find<DatePicker>(R.id.datePicker)

					val date = Calendar.getInstance().apply {
						set(Calendar.SECOND, 0)
						set(Calendar.MINUTE, 0)
						set(Calendar.HOUR_OF_DAY, 0)
					}
					val year = date.get(Calendar.YEAR)
					val month = date.get(Calendar.MONTH)
					val day = date.get(Calendar.DAY_OF_MONTH)
					picker.init(year, month, day) { _, y, m, d ->
						date.set(Calendar.YEAR, y)
						date.set(Calendar.MONTH, m)
						date.set(Calendar.DAY_OF_MONTH, d)
					}
					picker.maxDate = date.timeInMillis

					dialog.find<View>(R.id.buttonOk).setOnClickListener {
						callbackOk.invoke(date)
						dialog.dismiss()
					}
				}, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
			}
		}
	}

	private fun showDayCountInfo(pair: Pair<Item, MutableList<Item>?>) {
		create(activity!!, R.layout.dialog_day_count_info, { dialog ->
			dialog.find<RecyclerView>(R.id.listDayCountMonthly).apply {
				layoutManager = LinearLayoutManager(context!!, RecyclerView.VERTICAL, false)
				listDayCountMonthlyAdapter = ListDayCountMonthlyAdapter(pair)
				adapter = listDayCountMonthlyAdapter
			}

			dialog.find<AppCompatTextView>(R.id.textTitle).text = pair.first.title
			dialog.find<AppCompatTextView>(R.id.textDescription).text =
				resources.let {
					it.getString(
						R.string.item_description,
						it.getString(R.string.day_count_this_year),
						pair.second!!.size.toString()
					)
				}
		}, Utils.UNDEFINED_WINDOW_SIZE, ViewGroup.LayoutParams.WRAP_CONTENT)
	}

	private fun showPercentageInfo(pair: Pair<Item?, List<Float>?>) {
		create(activity!!, R.layout.dialog_percentage_info, { dialog ->
			val average = pair.second!!.sum() / pair.second!!.size
			val min = pair.second!!.min()!!
			val max = pair.second!!.max()!!
			val diffMin = min - pair.first!!.amount
			val diffMax = max - pair.first!!.amount

			dialog.apply {
				find<AppCompatTextView>(R.id.textTitle).text = pair.first?.title
				find<AppCompatTextView>(R.id.textDescription).text =
					resources.let {
						it.getString(
							R.string.item_description,
							it.getString(R.string.average_time_spent),
							it.getString(R.string.timeDiff, average.toInt(), ((average - average.toInt()) * 60).toInt())
						)
					}
				find<AppCompatTextView>(R.id.textTimeMin).text =
					resources.getString(
						R.string.time,
						min.toInt(),
						((min - min.toInt()) * 60).toInt()
					)
				find<AppCompatTextView>(R.id.textTimeDiffMin).text =
					resources.getString(
						R.string.timeDiff,
						diffMin.toInt(),
						((diffMin - diffMin.toInt()) * 60).toInt()
					)
				find<AppCompatTextView>(R.id.textTimeMax).text =
					resources.getString(
						R.string.time,
						max.toInt(),
						((max - max.toInt()) * 60).toInt()
					)
				find<AppCompatTextView>(R.id.textTimeDiffMax).text =
					resources.getString(
						R.string.timeDiff,
						diffMax.toInt(),
						((diffMax - diffMax.toInt()) * 60).toInt()
					)
			}
		}, Utils.UNDEFINED_WINDOW_SIZE, ViewGroup.LayoutParams.WRAP_CONTENT)
	}

	fun setDb(db: AppDatabase) {
		this.db = db
	}
}