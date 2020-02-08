package com.pointlessapss.timecontroler.fragments

import android.view.ViewGroup
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.charts.*
import com.pointlessapss.timecontroler.models.Item
import org.jetbrains.anko.find

class FragmentAnalyticsPage(
	private val parent: Item?,
	private val tasksByParent: Map<Item?, MutableList<Item>?>
) : FragmentBase() {

	var onParentChangeListener: ((Item?) -> Unit)? = null

	override fun getLayoutId() = R.layout.fragment_analytics_page

	override fun created() {
		setCharts()
	}

	private fun setCharts() {
		val layout = rootView!!.find<ViewGroup>(R.id.container)
		layout.removeAllViews()
		tasksByParent[parent]?.also { tasks ->
			if (parent!!.prize != null) {
				layout.addView(ChartSalary(requireContext(), parent, tasks).apply {
					onParentChangeListener = {
						this@FragmentAnalyticsPage.onParentChangeListener?.invoke(it)
					}
				})
			}
			if (!parent.wholeDay) {
				layout.addView(ChartHours(requireContext(), parent, tasks))
			}
			if (parent.amount != 0f) {
				layout.addView(ChartTimeSpent(requireContext(), parent, tasks))
			}
			layout.addView(ChartDayCount(requireContext(), parent, tasks))

			if (!parent.weekdays.any { !it }) {
				layout.addView(ChartStreak(requireContext(), parent, tasks))
			}
		}
	}
}