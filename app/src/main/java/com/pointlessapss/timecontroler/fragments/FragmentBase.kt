package com.pointlessapss.timecontroler.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.pointlessapss.timecontroler.R

abstract class FragmentBase : Fragment() {

	var rootView: ViewGroup? = null

	var onForceRefreshListener: (() -> Unit)? = null
	var onChangeFragmentListener: ((FragmentBase) -> Unit)? = null
	var forceRefresh = false

	@LayoutRes abstract fun getLayoutId(): Int
	abstract fun created()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		if (rootView == null || forceRefresh) {
			forceRefresh = false
			rootView = inflater.inflate(getLayoutId(), container, false) as ViewGroup

			created()
		}
		return rootView
	}
}