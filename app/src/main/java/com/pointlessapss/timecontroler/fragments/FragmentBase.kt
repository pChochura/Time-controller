package com.pointlessapss.timecontroler.fragments

import androidx.fragment.app.Fragment

open class FragmentBase : Fragment() {

	var onForceRefreshListener: (() -> Unit)? = null
	var forceRefresh = false

	fun isRefreshForced() = if (forceRefresh) {
		forceRefresh = false
		true
	} else {
		false
	}
}