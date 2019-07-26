package com.pointlessapss.timecontroler.models

import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import java.util.*

data class Item(val title: String, var type: ItemType = ItemType.TimeBased) {

	var startDate: Calendar? = null

	val id = UUID.randomUUID().hashCode()

	@ColorInt
	var color: Int = Random().nextInt()

	@FloatRange(from = 0.0)
	var defaultAmount = 0.0f

	@FloatRange(from = 0.0)
	var amount = 0.0f

	val defaultTimeAmount
		get() = getTimeAmount(defaultAmount)

	fun getTimeAmount(amount: Float = this.amount) =
		"${amount.toInt()}:${String.format("%02d", ((amount - amount.toInt()) * 60).toInt())}"

	fun getPercentage() = (amount / defaultAmount * 100).toInt()
}