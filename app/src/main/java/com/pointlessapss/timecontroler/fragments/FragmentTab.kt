package com.pointlessapss.timecontroler.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment

class FragmentTab : Fragment() {

	private val maxOptions = 4

	private var rootView: ViewGroup? = null
	var layoutId: Int = 0

	private lateinit var listener: (layout: FrameLayout, pos: Int) -> Unit

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		if (rootView == null) {
			rootView = inflater.inflate(layoutId, container, false) as ViewGroup

			handleClicks()
		}
		return rootView
	}

	private fun handleClicks() {
		for (i in 1..maxOptions) {
			val id = rootView?.context?.resources?.getIdentifier("option$i", "id", rootView?.context?.packageName)
			id?.also {
				rootView?.findViewById<FrameLayout>(id)?.setOnClickListener {
					listener.invoke(it as FrameLayout, i)
				}
			}
		}
	}

	fun setOnOptionSelectedListener(listener: (layout: FrameLayout, pos: Int) -> Unit) {
		this.listener = listener
	}
}