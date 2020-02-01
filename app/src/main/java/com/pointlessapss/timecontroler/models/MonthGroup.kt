package com.pointlessapss.timecontroler.models

import com.pointlessapss.timecontroler.utils.Utils
import java.util.*

class MonthGroup(item: Item) : Comparable<MonthGroup> {

	internal val month = item.startDate!!.get(Calendar.MONTH)
	internal val year = item.startDate!!.get(Calendar.YEAR)

	val calendar: Calendar
		get() = Utils.date.apply {
			set(Calendar.MONTH, month)
			set(Calendar.YEAR, year)
		}

	fun getIndex() = year * 12f + month.toFloat()

	override fun compareTo(other: MonthGroup) = compareValuesBy(this, other, { year }, { month })

	override fun equals(other: Any?): Boolean {
		if (other !is MonthGroup) {
			return false
		}
		if (other.hashCode() != hashCode()) {
			return false
		}
		if (other.year != year || other.month != month) {
			return false
		}
		return true
	}

	override fun hashCode(): Int {
		var result = month
		result = 31 * result + year
		return result
	}
}