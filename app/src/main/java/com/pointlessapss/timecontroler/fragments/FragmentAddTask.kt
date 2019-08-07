package com.pointlessapss.timecontroler.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.utils.DialogUtil
import com.pointlessapss.timecontroler.utils.Utils
import org.jetbrains.anko.find

class FragmentAddTask(private val item: Item = Item()) : BottomSheetDialogFragment() {

	private var rootView: ViewGroup? = null
	private lateinit var saveListener: (Item) -> Unit

	override fun getTheme() = R.style.AppBottomSheetDialogTheme

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_add_task, container, false) as ViewGroup

			handleClicks()
			rootView!!.find<AppCompatEditText>(R.id.textTaskName).setText(item.title)
			FragmentOptions.handleOptions(activity!!, rootView!!, item)
		}
		return rootView
	}

	fun setSaveListener(saveListener: (Item) -> Unit) {
		this.saveListener = saveListener
	}

	private fun handleClicks() {
		rootView?.find<View>(R.id.buttonSave)?.setOnClickListener {
			val textTaskName = rootView?.find<AppCompatEditText>(R.id.textTaskName)

			item.title = textTaskName?.text.toString()

			if (item.title.isBlank()) {
				DialogUtil.showMessage(activity!!, resources.getString(R.string.message_empty_title))
				return@setOnClickListener
			}

			saveListener.invoke(item)

			dismiss()
		}
	}
}