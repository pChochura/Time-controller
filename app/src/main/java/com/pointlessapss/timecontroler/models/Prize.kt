package com.pointlessapss.timecontroler.models

import android.content.Context
import com.pointlessapss.timecontroler.R

class Prize(var type: Type, var amount: Float) {
	enum class Type(val id: Int) {
		PER_MONTH(R.id.per_month), PER_DAY(R.id.per_day), PER_HOUR(R.id.per_hour), PER_TASK(R.id.per_task);

		companion object {
			fun fromId(id: Int) = values().find { it.id == id } ?: PER_TASK
		}
	}

	fun describe(context: Context): String {
		val id = context.resources.getIdentifier(type.toString().toLowerCase(), "string", context.packageName)
		return "${context.resources.getString(id)}: $amount"
	}
}