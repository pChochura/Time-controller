package com.pointlessapss.timecontroler.utils

import java.util.*

enum class Period {
	THIS_WEEK, THIS_MONTH, SINCE_LAST_SETTLEMENT, CUSTOM;

	private var start: Calendar? = null
	private var end: Calendar? = null

	fun with(start: Calendar, end: Calendar) {
		this.start = start
		this.end = end
	}

	fun meetsCondition(date: Calendar): Boolean {
		val today = Utils.date
		return when (this) {
			THIS_WEEK -> date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
					date.get(Calendar.WEEK_OF_YEAR) == today.get(Calendar.WEEK_OF_YEAR)
			THIS_MONTH -> date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
					date.get(Calendar.MONTH) == today.get(Calendar.MONTH)
			SINCE_LAST_SETTLEMENT -> true
			CUSTOM -> date.isBetween(start, end)
		}
	}
}

fun Calendar?.isBetween(startDate: Calendar?, endDate: Calendar?): Boolean {
	return this?.compareTo(startDate ?: return false) ?: -1 >= 0 &&
			this?.compareTo(endDate ?: return false) ?: 1 <= 0
}