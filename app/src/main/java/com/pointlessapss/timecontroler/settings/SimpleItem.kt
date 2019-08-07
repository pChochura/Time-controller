package com.pointlessapss.timecontroler.settings

import android.content.Context

class SimpleItem(context: Context) : SettingsItem<Unit>(context) {

	override fun getType() = Type.SIMPLE

	init {
		setOnClickListener { onClickListener.invoke(Unit) }
	}
}
