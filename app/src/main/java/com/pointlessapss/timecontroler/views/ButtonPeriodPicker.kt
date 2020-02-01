package com.pointlessapss.timecontroler.views

import android.view.View
import android.view.ViewGroup
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.utils.DialogUtil
import com.pointlessapss.timecontroler.utils.Period
import com.pointlessapss.timecontroler.utils.Utils

class ButtonPeriodPicker(
	val button: View,
	availablePeriods: Array<Period> = arrayOf(
		Period.THIS_WEEK,
		Period.THIS_MONTH,
		Period.SINCE_LAST_SETTLEMENT,
		Period.CUSTOM
	),
	val onPeriodSelectedListener: ((Period) -> Unit)? = null
) {

	init {
		button.setOnClickListener {
			DialogUtil.create(button.context, R.layout.dialog_picker_period, { dialog ->

			}, Utils.UNDEFINED_WINDOW_SIZE, ViewGroup.LayoutParams.WRAP_CONTENT)
		}
	}
}