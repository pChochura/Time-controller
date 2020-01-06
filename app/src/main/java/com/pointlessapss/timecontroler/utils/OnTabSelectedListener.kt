package com.pointlessapss.timecontroler.utils

import com.google.android.material.tabs.TabLayout

fun TabLayout.addOnTabSelectedListener(onTabSelectedListener: (TabLayout.Tab?) -> Unit) {
	addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
		override fun onTabReselected(tab: TabLayout.Tab?) = Unit
		override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
		override fun onTabSelected(tab: TabLayout.Tab?) = onTabSelectedListener.invoke(tab)
	})
}