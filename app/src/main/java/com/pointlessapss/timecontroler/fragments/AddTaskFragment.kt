package com.pointlessapss.timecontroler.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pointlessapss.timecontroler.R

class AddTaskFragment : BottomSheetDialogFragment() {

	private var rootView: ViewGroup? = null

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_add_task, container, false) as ViewGroup


		}
		return rootView
	}
}