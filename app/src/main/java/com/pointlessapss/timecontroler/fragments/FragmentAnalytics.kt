package com.pointlessapss.timecontroler.fragments

import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.pointlessapss.timecontroler.R
import com.github.mikephil.charting.components.Legend.LegendForm
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.YAxis.YAxisLabelPosition
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import org.jetbrains.anko.find
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.model.GradientColor
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.charts.CandleStickChart
import com.github.mikephil.charting.data.*
import com.pointlessapss.timecontroler.database.AppDatabase
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*

class FragmentAnalytics : Fragment() {

	private var rootView: ViewGroup? = null

	private lateinit var db: AppDatabase

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_analytics, container, false) as ViewGroup

			val candleStickChart = rootView!!.find<CandleStickChart>(R.id.chartRange)
			candleStickChart.isHighlightPerDragEnabled = true

			candleStickChart.setDrawBorders(true)

			candleStickChart.setBorderColor(resources.getColor(R.color.color1))

			val yAxis = candleStickChart.axisLeft
			val rightAxis = candleStickChart.axisRight
			yAxis.setDrawGridLines(false)
			rightAxis.setDrawGridLines(false)
			candleStickChart.requestDisallowInterceptTouchEvent(true)

			val xAxis = candleStickChart.xAxis

			xAxis.setDrawGridLines(true)// disable x axis grid lines
			xAxis.setDrawLabels(true)
			rightAxis.textColor = Color.BLACK
			yAxis.setDrawLabels(false)
			xAxis.granularity = 1f
			xAxis.isGranularityEnabled = true
			xAxis.setAvoidFirstLastClipping(true)

//			candleStickChart.axisRight.

			doAsync {
				val yValsCandleStick = mutableListOf<CandleEntry>()
				yValsCandleStick.addAll(db.itemDao().getAll(true).filter {
					it.startDate?.get(Calendar.MONTH) == Calendar.JULY
				}.map {
					CandleEntry(
						it.startDate!!.get(Calendar.DAY_OF_MONTH).toFloat(),
						it.startDate!!.get(Calendar.HOUR_OF_DAY) + it.amount,
						it.startDate!!.get(Calendar.HOUR_OF_DAY).toFloat(),
						it.startDate!!.get(Calendar.HOUR_OF_DAY) + it.amount,
						it.startDate!!.get(Calendar.HOUR_OF_DAY).toFloat()
					)
				})

				val set1 = CandleDataSet(yValsCandleStick, "DataSet 1")
				set1.color = Color.rgb(80, 80, 80)
				set1.shadowColor = resources.getColor(R.color.color11)
				set1.shadowWidth = 0.8f
				set1.decreasingColor = resources.getColor(R.color.color14)
				set1.decreasingPaintStyle = Paint.Style.FILL
				set1.increasingColor = resources.getColor(R.color.colorAccent)
				set1.increasingPaintStyle = Paint.Style.FILL
				set1.neutralColor = Color.LTGRAY
				set1.setDrawValues(false)

				val data = CandleData(set1)

				uiThread {
					candleStickChart.data = data
					candleStickChart.invalidate()
				}
			}
		}
		return rootView
	}

	fun setDb(db: AppDatabase) {
		this.db = db
	}
}
