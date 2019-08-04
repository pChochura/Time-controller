package com.pointlessapss.timecontroler.models

import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "items")
class Item(@ColumnInfo(name = "title") var title: String = "") {

	@PrimaryKey var id = UUID.randomUUID().hashCode()

	@ColumnInfo(name = "startDate") var startDate: Calendar? = null
	@ColumnInfo(name = "weekdays") var weekdays = BooleanArray(7)
	@ColumnInfo(name = "color") @ColorInt var color = 0
	@ColumnInfo(name = "amount") @FloatRange(from = 0.0) var amount = 0.0f
	@ColumnInfo(name = "done") var done: Boolean = false

	fun getTimeAmount() =
		"${amount.toInt()}:${String.format("%02d", ((amount - amount.toInt()) * 60).toInt())}"

	fun set(item: Item, date: Calendar) {
		id = item.id
		title = item.title
		startDate = ((if (item.startDate == null) {
			date
		} else {
			item.startDate!!
		}).clone() as Calendar)
			.apply {
				set(Calendar.DAY_OF_YEAR, date.get(Calendar.DAY_OF_YEAR))
				set(Calendar.YEAR, date.get(Calendar.YEAR))
			}
		amount = item.amount
		color = item.color
		item.weekdays.copyInto(weekdays)
	}
}