package com.pointlessapss.timecontroler.utils

import com.pointlessapss.timecontroler.models.Item
import java.util.*

enum class Period {
	THIS_WEEK, THIS_MONTH, SINCE_LAST_SETTLEMENT;

	fun meetsCondition(date: Calendar, parentTask: Item? = null): Boolean {
		val today = Calendar.getInstance().apply {
			firstDayOfWeek = date.firstDayOfWeek
		}
		return when (this) {
			THIS_WEEK -> date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
					date.get(Calendar.WEEK_OF_YEAR) == today.get(Calendar.WEEK_OF_YEAR)
			THIS_MONTH -> date.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
					date.get(Calendar.MONTH) == today.get(Calendar.MONTH)
			SINCE_LAST_SETTLEMENT -> true
		}
	}

	companion object {
		fun isBetween(task: Item, startDate: Calendar, endDate: Calendar) =
			task.startDate?.compareTo(startDate) ?: -1 >= 0 && task.startDate?.compareTo(endDate) ?: 1 <= 0
	}
}