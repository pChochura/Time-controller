package com.pointlessapss.timecontroler.utils

import android.graphics.Canvas
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler

open class RoundedBarChartRenderer(
	chart: BarChart,
	animator: ChartAnimator,
	vpHandler: ViewPortHandler,
	private val cornerDimens: Float
) : BarChartRenderer(chart, animator, vpHandler) {

	init {
		initBuffers()
	}

	override fun drawDataSet(c: Canvas, dataSet: IBarDataSet, index: Int) {
		val trans = mChart.getTransformer(dataSet.axisDependency)

		mBarBorderPaint.color = dataSet.barBorderColor
		mBarBorderPaint.strokeWidth = Utils.convertDpToPixel(dataSet.barBorderWidth)

		val phaseX = mAnimator.phaseX
		val phaseY = mAnimator.phaseY

		// initialize the buffer
		val buffer = mBarBuffers[index]
		buffer.setPhases(phaseX, phaseY)
		buffer.setDataSet(index)
		buffer.setInverted(mChart.isInverted(dataSet.axisDependency))
		buffer.setBarWidth(mChart.barData.barWidth)

		buffer.feed(dataSet)

		trans.pointValuesToPixel(buffer.buffer)

		val isSingleColor = dataSet.colors.size == 1

		if (isSingleColor) {
			mRenderPaint.color = dataSet.color
		}

		var j = 0
		while (j < buffer.size()) {

			if (!mViewPortHandler.isInBoundsLeft(buffer.buffer[j + 2])) {
				j += 4
				continue
			}

			if (!mViewPortHandler.isInBoundsRight(buffer.buffer[j]))
				break

			if (!isSingleColor) {
				mRenderPaint.color = dataSet.getColor(j / 4)
			}

			val left = buffer.buffer[j] + (buffer.buffer[j + 2] - buffer.buffer[j]) * 0.3f
			val right = buffer.buffer[j + 2] - (buffer.buffer[j + 2] - buffer.buffer[j]) * 0.3f

			c.drawRoundRect(
				left, buffer.buffer[j + 1], right,
				buffer.buffer[j + 3], cornerDimens, cornerDimens, mRenderPaint
			)
			c.drawRect(
				left, buffer.buffer[j + 3] - cornerDimens, right,
				buffer.buffer[j + 3], mRenderPaint
			)
			j += 4
		}
	}

	override fun drawValue(c: Canvas, valueText: String, x: Float, y: Float, color: Int) {
		c.save()
		c.clipRect(mViewPortHandler.contentRect)
		super.drawValue(c, valueText, x, y, color)
		c.restore()
	}

	override fun drawValues(c: Canvas) {
		val valueTextHeight = Utils.calcTextHeight(mValuePaint, "8").toFloat()
		val valueOffsetPlus = Utils.convertDpToPixel(4.5f)

		for (i in 0 until mChart.barData.dataSetCount) {
			val dataSet = mChart.barData.getDataSetByIndex(i)
			val formatter = dataSet.valueFormatter
			val buffer = mBarBuffers[i]

			var posOffset =
				if (mChart.isDrawValueAboveBarEnabled) -valueOffsetPlus else valueTextHeight + valueOffsetPlus
			var negOffset =
				if (mChart.isDrawValueAboveBarEnabled) valueTextHeight + valueOffsetPlus else -valueOffsetPlus
			if (mChart.isInverted(dataSet.axisDependency)) {
				posOffset = -posOffset - valueTextHeight
				negOffset = -negOffset - valueTextHeight
			}
			var j = 0
			while (j < buffer.buffer.size * mAnimator.phaseX) {
				val entry = dataSet.getEntryForIndex(j / 4)
				if (entry.data as Boolean? != true) {
					val x = (buffer.buffer[j] + buffer.buffer[j + 2]) / 2f
					drawValue(
						c, formatter.getBarLabel(entry), x,
						if (entry.y >= 0)
							buffer.buffer[j + 1] + posOffset
						else
							buffer.buffer[j + 3] + negOffset,
						dataSet.getValueTextColor(j / 4)
					)
				}
				j += 4
			}
		}
	}
}
