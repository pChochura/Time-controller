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
import com.pointlessapss.timecontroler.utils.Utils

class FragmentAddTask : BottomSheetDialogFragment() {

	private var rootView: ViewGroup? = null
	private lateinit var saveListener: (Item) -> Unit

	private val item = Item()

	override fun getTheme() = R.style.AppBottomSheetDialogTheme

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_add_task, container, false) as ViewGroup

			handleClicks()
		}
		return rootView
	}

	fun setSaveListener(saveListener: (Item) -> Unit) {
		this.saveListener = saveListener
	}

	private fun handleClicks() {
		rootView?.findViewById<View>(R.id.buttonSave)?.setOnClickListener {
			val textTaskName = rootView?.findViewById<AppCompatEditText>(R.id.textTaskName)

			item.title = textTaskName?.text.toString()

			if (item.title.isBlank()) {
				showError(resources.getString(R.string.blank_title))
				return@setOnClickListener
			}

			saveListener.invoke(item)

			dismiss()
		}

		FragmentOptions.handleOptions(activity!!, rootView!!, item)
	}

	private fun showError(content: String) {
		Utils.makeDialog(activity!!, R.layout.dialog_message, { dialog ->

			dialog.findViewById<AppCompatTextView>(R.id.textContent).text = content

		}, Utils.UNDEFINED_WINDOW_SIZE, ViewGroup.LayoutParams.WRAP_CONTENT)
	}
}