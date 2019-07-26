package com.pointlessapss.timecontroler.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.models.ItemType
import org.apache.commons.lang3.StringUtils

class AddTaskFragment : BottomSheetDialogFragment() {

	private var rootView: ViewGroup? = null

	override fun getTheme() = R.style.AppBottomSheetDialogTheme

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_add_task, container, false) as ViewGroup

			setTabs()
		}
		return rootView
	}

	private fun setTabs() {
		val tabLayout = rootView?.findViewById<TabLayout>(R.id.tabLayoutType)
		val viewPager = rootView?.findViewById<ViewPager>(R.id.viewPagerType)

		tabLayout?.setupWithViewPager(viewPager)
		viewPager?.adapter = DemoCollectionPagerAdapter(childFragmentManager)
	}

	private fun onOptionSelected(tabPos: Int, optionPos: Int) {
		when (optionPos) {
			1 -> {
//				TODO: add datePicker
			}
			3 -> {
//				TODO: add colorPicker
			}
			else -> {

			}
		}
	}

	inner class DemoCollectionPagerAdapter(fm: FragmentManager) :
		FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

		override fun getCount(): Int = ItemType.values().size

		override fun getItem(position: Int): FragmentTab {
			return FragmentTab().apply {
				layoutId = when (position) {
					0 -> R.layout.fragment_time_based
					else -> R.layout.fragment_count_based
				}
				setOnOptionSelectedListener { optionPos ->
					onOptionSelected(position, optionPos)
				}
			}
		}

		override fun getPageTitle(position: Int): String = StringUtils.join(
			StringUtils.splitByCharacterTypeCamelCase(ItemType.values()[position].toString()),
			' '
		)
	}
}