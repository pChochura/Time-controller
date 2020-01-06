package com.pointlessapss.timecontroler.utils

import android.graphics.*
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

	override fun drawDataSet(c: Canvas, dataSet: IBarDataSet, index: Int) {
		val trans = mChart.getTransformer(dataSet.axisDependency)

		mBarBorderPaint.color = dataSet.barBorderColor
		mBarBorderPaint.strokeWidth = Utils.convertDpToPixel(dataSet.barBorderWidth)

		val drawBorder = dataSet.barBorderWidth > 0f

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
				// Set the color for the currently drawn value. If the index
				// is out of bounds, reuse colors.
				mRenderPaint.color = dataSet.getColor(j / 4)
			}

			if (dataSet.gradientColor != null) {
				val gradientColor = dataSet.gradientColor
				mRenderPaint.shader = LinearGradient(
					buffer.buffer[j],
					buffer.buffer[j + 3],
					buffer.buffer[j],
					buffer.buffer[j + 1],
					gradientColor.startColor,
					gradientColor.endColor,
					Shader.TileMode.MIRROR
				)
			}

			if (dataSet.gradientColors != null) {
				mRenderPaint.shader = LinearGradient(
					buffer.buffer[j],
					buffer.buffer[j + 3],
					buffer.buffer[j],
					buffer.buffer[j + 1],
					dataSet.getGradientColor(j / 4).startColor,
					dataSet.getGradientColor(j / 4).endColor,
					Shader.TileMode.MIRROR
				)
			}


			c.drawRoundRect(
				buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
				buffer.buffer[j + 3], cornerDimens, cornerDimens, mRenderPaint
			)

			if (drawBorder) {
				c.drawRoundRect(
					buffer.buffer[j], buffer.buffer[j + 1], buffer.buffer[j + 2],
					buffer.buffer[j + 3], cornerDimens, cornerDimens, mBarBorderPaint
				)
			}
			j += 4
		}
	}
}
