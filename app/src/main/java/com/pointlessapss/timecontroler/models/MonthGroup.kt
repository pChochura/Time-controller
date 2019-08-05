package com.pointlessapss.timecontroler.models

import java.util.*

class MonthGroup(private val month: Int, private val year: Int, val title: String) : Comparable<MonthGroup> {
	val calendar: Calendar
		get() = Calendar.getInstance().apply {
			set(Calendar.MONTH, month)
			set(Calendar.YEAR, year)
		}

	override fun compareTo(other: MonthGroup) = compareValuesBy(this, other, { title }, { year }, { month })

	override fun equals(other: Any?): Boolean {
		if (other !is MonthGroup) {
			return false
		}
		if (other.hashCode() != hashCode()) {
			return false
		}
		if (other.title != title || other.year != year || other.month != month) {
			return false
		}
		return true
	}

	override fun hashCode(): Int {
		var result = month
		result = 31 * result + year
		result = 31 * result + title.hashCode()
		return result
	}
}