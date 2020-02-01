package com.pointlessapss.timecontroler.models

import com.pointlessapss.timecontroler.utils.Utils
import java.util.*

class WeekGroup(item: Item) : Comparable<WeekGroup> {

	internal val week = item.startDate!!.get(Calendar.WEEK_OF_YEAR)
	internal val year = item.startDate!!.get(Calendar.YEAR)

	val calendar: Calendar
		get() = Utils.date.apply {
			set(Calendar.WEEK_OF_YEAR, week)
			set(Calendar.YEAR, year)
		}

	fun getIndex() = year * 52f + week.toFloat()

	override fun compareTo(other: WeekGroup) = compareValuesBy(this, other, { year }, { week })

	override fun equals(other: Any?): Boolean {
		if (other !is WeekGroup) {
			return false
		}
		if (other.hashCode() != hashCode()) {
			return false
		}
		if (other.year != year || other.week != week) {
			return false
		}
		return true
	}

	override fun hashCode(): Int {
		var result = week
		result = 31 * result + year
		return result
	}
}