package com.pointlessapss.timecontroler.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatEditText
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.utils.DialogUtil
import org.jetbrains.anko.find

class FragmentAddTask(private val rootItem: Item? = null) : BottomSheetDialogFragment() {

	private val item = rootItem?.let { Item().apply { set(it) } } ?: Item()

	private var rootView: ViewGroup? = null
	private lateinit var saveListener: (Item) -> Unit

	override fun getTheme() = R.style.AppBottomSheetDialogTheme

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_add_task, container, false) as ViewGroup

			dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

			handleClicks()
			rootView!!.find<AppCompatEditText>(R.id.textTaskName).setText(item.title)
			FragmentOptions.handleOptions(activity!!, rootView!!, item)
		}
		return rootView
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
			setOnShowListener { d ->
				val bottomSheet = (d as BottomSheetDialog).find<View>(R.id.design_bottom_sheet) as FrameLayout?
				BottomSheetBehavior.from(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED
			}
		}
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

			saveListener.invoke(rootItem?.apply { set(item) } ?: item)

			dismiss()
		}
	}
}