package com.pointlessapss.timecontroler.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.utils.Utils
import org.jetbrains.anko.find
import java.util.*

class FragmentDayOptions(private val day: Calendar) : BottomSheetDialogFragment() {

	private var rootView: ViewGroup? = null

	lateinit var clickListener: ClickListener

	override fun getTheme() = R.style.AppBottomSheetDialogTheme

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_day_options, container, false) as ViewGroup

			rootView!!.find<AppCompatTextView>(R.id.textTitle).text = Utils.formatDateWeekday.format(day.time)

			handleClicks()
		}
		return rootView
	}

	private fun handleClicks() {
		rootView!!.apply {
			find<View>(R.id.optionMarkDisabled).setOnClickListener {
				clickListener.onMarkDsiabled()
			}
			find<View>(R.id.optionRemoveAll).setOnClickListener {
				clickListener.onRemoveAll()
			}
		}
	}

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		return (super.onCreateDialog(savedInstanceState) as BottomSheetDialog).apply {
			setOnShowListener { d ->
				val bottomSheet = (d as BottomSheetDialog).find<View>(R.id.design_bottom_sheet) as FrameLayout?
				BottomSheetBehavior.from(bottomSheet!!).state = BottomSheetBehavior.STATE_EXPANDED
			}
		}
	}

	public interface ClickListener {
		fun onMarkDsiabled()
		fun onRemoveAll()
	}
}