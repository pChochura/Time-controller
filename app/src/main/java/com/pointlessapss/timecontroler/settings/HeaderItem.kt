package com.pointlessapss.timecontroler.settings

import android.content.Context

import com.pointlessapss.timecontroler.R

class HeaderItem(context: Context) : SettingsItem<Unit>(context) {

	init {
		initializeLayout(R.layout.item_header)

		textTitle = findViewById(R.id.textTitle)
	}

	override fun hasRipple() = false
}
