package com.pointlessapss.timecontroler.models

import java.util.*

class MonthGroup(val item: Item) : Comparable<MonthGroup> {

	private val month = item.startDate!!.get(Calendar.MONTH)
	private val year = item.startDate!!.get(Calendar.YEAR)

	val calendar: Calendar
		get() = Calendar.getInstance().apply {
			set(Calendar.MONTH, month)
			set(Calendar.YEAR, year)
		}

	override fun compareTo(other: MonthGroup) = compareValuesBy(this, other, { item.title }, { year }, { month })

	override fun equals(other: Any?): Boolean {
		if (other !is MonthGroup) {
			return false
		}
		if (other.hashCode() != hashCode()) {
			return false
		}
		if (other.item.title != item.title || other.year != year || other.month != month) {
			return false
		}
		return true
	}

	override fun hashCode(): Int {
		var result = month
		result = 31 * result + year
		result = 31 * result + item.title.hashCode()
		return result
	}
}