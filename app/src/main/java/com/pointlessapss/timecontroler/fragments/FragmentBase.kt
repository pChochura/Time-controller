package com.pointlessapss.timecontroler.fragments

import androidx.fragment.app.Fragment

open class FragmentBase : Fragment() {

	private var onForceRefreshListener: (() -> Unit)? = null
	var forceRefresh = false

	fun setOnForceRefreshListener(onForceRefreshListener: () -> Unit) {
		this.onForceRefreshListener = onForceRefreshListener
	}

	fun isRefreshForced() = if (forceRefresh) {
		forceRefresh = false
		true
	} else {
		false
	}
}