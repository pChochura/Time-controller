package com.pointlessapss.timecontroler.charts

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.models.Prize
import com.pointlessapss.timecontroler.utils.DialogUtil
import com.pointlessapss.timecontroler.utils.Utils
import com.pointlessapss.timecontroler.utils.ValueFormatters
import com.pointlessapss.timecontroler.views.MonthPickerView
import kotlinx.android.synthetic.main.chart_salary.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import java.util.*
import kotlin.math.absoluteValue

class ChartSalary(
	context: Context,
	private val parent: Item?,
	private val tasks: List<Item>
) : FrameLayout(context) {

	var onParentChangeListener: ((Item?) -> Unit)? = null

	constructor(context: Context) : this(context, null, listOf())

	init {
		View.inflate(context, R.layout.chart_salary, this).post {
			textSalary.text =
				context.getString(R.string.salary, Prize.getPrizeSumSinceLast(parent!!, tasks), "")

			initChart()
			showChart()
			setInfoBar()
		}
	}

	private fun initChart() {
		chartSalary.apply {
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
				valueFormatter = ValueFormatters.formatterMonth
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
		val values = mutableListOf<Entry?>()

		val now = Utils.date
		val first = tasks.minBy { it.startDate ?: now }?.startDate!!
		for (i in 0 until (parent!!.settlements?.size ?: 0)) {
			val end = if (i == 0) first else parent.settlements!![i - 1]
			values.add(
				Entry(
					end.get(Calendar.MONTH) + end.get(Calendar.YEAR) * 12f,
					Prize.getPrizeSum(parent.prize, tasks.filter {
						(it.startDate?.before(parent.settlements!![i]) ?: true) &&
								(i == 0 || it.startDate?.after(parent.settlements!![i - 1]) ?: true)
					})!!.toFloat().absoluteValue
				)
			)
		}
		val salarySinceSettlement = Prize.getPrizeSumSinceLast(parent, tasks)!!.toFloat()
		if (salarySinceSettlement != 0.0f) {
			values.add(
				Entry(
					now.get(Calendar.MONTH) + now.get(Calendar.YEAR) * 12f,
					salarySinceSettlement
				)
			)
		}

		val last = parent.settlements?.max()!!
		chartSalary.apply {
			this.data = LineData(LineDataSet(values, this@ChartSalary.parent.title).apply {
				valueTypeface = ResourcesCompat.getFont(context, R.font.lato)
				valueTextSize = 10f
				valueFormatter = ValueFormatters.formatterNumber
				valueTextColor = ContextCompat.getColor(context, R.color.colorText3)
				mode = LineDataSet.Mode.CUBIC_BEZIER
				color = this@ChartSalary.parent.color
				fillColor = this@ChartSalary.parent.color
				setCircleColor(this@ChartSalary.parent.color)
				setDrawCircleHole(false)
				setDrawFilled(true)
			})
			setVisibleXRange(1f, 6f)
			moveViewToX(last.get(Calendar.MONTH) + last.get(Calendar.YEAR) * 12f)
		}.invalidate()
	}

	private fun showPeriodPickerDialog(prizeType: Prize.Type, callbackOk: (Calendar) -> Unit) {
		when (prizeType) {
			Prize.Type.PER_MONTH -> {
				DialogUtil.create(context, R.layout.dialog_picker_month, { dialog ->
					dialog.find<View>(R.id.buttonOk).setOnClickListener {
						callbackOk.invoke(dialog.find<MonthPickerView>(R.id.monthPicker).selectedDate)
						dialog.dismiss()
					}
				}, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
			}
			else -> {
				DialogUtil.create(context, R.layout.dialog_picker_date, { dialog ->
					val picker = dialog.find<DatePicker>(R.id.datePicker)

					val date = Utils.date.apply {
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

	private fun setInfoBar() {
		val color = this@ChartSalary.parent?.color!!
		iconText.apply {
			backgroundTintList = ColorStateList.valueOf(
				ColorUtils.setAlphaComponent(
					color,
					50
				)
			)
			setTextColor(
				if (Utils.getLuminance(color) > 0.5f) {
					ColorUtils.blendARGB(color, Color.BLACK, 0.5f)
				} else {
					color
				}
			)
			text = Utils.getInitials(this@ChartSalary.parent.title)
		}
		textTitle.text = parent.title
		textUnitSalary.text = context.resources.getString(
			R.string.salary,
			parent.prize?.amount,
			parent.prize?.type?.asText(context)?.toLowerCase(Locale.getDefault())
		)
		buttonAddSettlement.setOnClickListener {
			showPeriodPickerDialog(parent.prize?.type!!) {
				if (parent.settlements == null) {
					parent.settlements = mutableListOf(it)
				} else {
					parent.settlements?.add(it)
				}
				doAsync {
					onParentChangeListener?.invoke(parent)
				}
				showChart()
			}
		}
	}
}