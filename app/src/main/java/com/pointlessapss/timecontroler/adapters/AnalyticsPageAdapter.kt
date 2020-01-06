package com.pointlessapss.timecontroler.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.pointlessapss.timecontroler.fragments.FragmentAnalyticsPage
import com.pointlessapss.timecontroler.models.Item

class AnalyticsPageAdapter(
	fm: FragmentManager,
	private val tasksByParent: Map<Item?, MutableList<Item>?>
) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

	var onParentChangeListener: ((Item?) -> Unit)? = null

	override fun getItem(position: Int): Fragment {
		return FragmentAnalyticsPage(tasksByParent.keys.toList()[position], tasksByParent).apply {
			onParentChangeListener = {
				this@AnalyticsPageAdapter.onParentChangeListener?.invoke(it)
			}
		}
	}

	override fun getCount() = tasksByParent.size
}