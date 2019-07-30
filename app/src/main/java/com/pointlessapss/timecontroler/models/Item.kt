package com.pointlessapss.timecontroler.models

import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import java.util.*

data class Item(var title: String = "") {

	var startDate: Calendar? = null

	val id = UUID.randomUUID().hashCode()

	val defaultWeekdays = BooleanArray(7)

	@ColorInt
	var color: Int = Random().nextInt()

	@FloatRange(from = 0.0)
	var defaultAmount = 0.0f

	@FloatRange(from = 0.0)
	var amount = 0.0f

	fun getTimeAmount() =
		"${amount.toInt()}:${String.format("%02d", ((amount - amount.toInt()) * 60).toInt())}"

	fun set(item: Item, date: Calendar) {
		title = item.title
		startDate = item.startDate
		startDate?.set(Calendar.DAY_OF_YEAR, date.get(Calendar.DAY_OF_YEAR))
		startDate?.set(Calendar.YEAR, date.get(Calendar.YEAR))
		amount = item.defaultAmount
	}
}