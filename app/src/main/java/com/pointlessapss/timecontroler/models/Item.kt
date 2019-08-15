package com.pointlessapss.timecontroler.models

import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentSnapshot
import com.google.gson.Gson
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Entity(tableName = "items")
class Item(@ColumnInfo(name = "title") var title: String = "") {

	@PrimaryKey var id = UUID.randomUUID().hashCode()

	@ColumnInfo(name = "parentId") var parentId: Int? = null
	@ColumnInfo(name = "startDate") var startDate: Calendar? = null
	@ColumnInfo(name = "weekdays") var weekdays = BooleanArray(7)
	@ColumnInfo(name = "color") @ColorInt var color = 0
	@ColumnInfo(name = "amount") @FloatRange(from = 0.0) var amount = 0.0f
	@ColumnInfo(name = "wholeDay") var wholeDay: Boolean = true
	@ColumnInfo(name = "prize") var prize: Prize? = null
	@ColumnInfo(name = "tags") var tags: IntArray? = null
	@ColumnInfo(name = "done") var done: Boolean = false
	@ColumnInfo(name = "settlements") var settlements: MutableList<Calendar>? = null

	fun getTimeAmount(amt: Float = amount) =
		"${amt.toInt()}:${String.format("%02d", ((amt - amt.toInt()) * 60).toInt())}"

	fun setParent(item: Item, date: Calendar) {
		set(item)
		parentId = item.id
		id = UUID.randomUUID().hashCode()
		startDate = ((if (item.startDate == null) {
			date
		} else {
			item.startDate!!
		}).clone() as Calendar)
			.apply {
				set(Calendar.DAY_OF_YEAR, date.get(Calendar.DAY_OF_YEAR))
				set(Calendar.YEAR, date.get(Calendar.YEAR))
			}
	}

	fun set(item: Item) {
		id = item.id
		parentId = item.parentId
		startDate = item.startDate?.let { it.clone() as Calendar }
		title = item.title
		amount = item.amount
		color = item.color
		wholeDay = item.wholeDay
		prize = item.prize
		tags = item.tags
		done = item.done
		item.weekdays.copyInto(weekdays)
		if (item.settlements != null) {
			settlements = mutableListOf(*item.settlements!!.toTypedArray())
		}
	}

	fun toMap(): Map<String, Any?> {
		return mutableMapOf(
			"id" to id,
			"parentId" to parentId,
			"title" to title,
			"startDate" to startDate?.timeInMillis,
			"weekdays" to weekdays.toList(),
			"color" to color,
			"amount" to amount,
			"wholeDay" to wholeDay,
			"prize" to prize,
			"tags" to tags,
			"done" to done,
			"settlements" to settlements
		)
	}

	companion object {
		fun fromDocument(doc: DocumentSnapshot): List<Item>? {
			return doc.data?.entries?.map { entry ->
				val item = entry.value as Map<*, *>
				Item(item["title"].toString()).apply {
					id = item["id"].toString().toInt()
					parentId = item["parentId"].toString().toIntOrNull()
					weekdays = (item["weekdays"] as ArrayList<*>).map { it.toString().toBoolean() }.toBooleanArray()
					startDate = Calendar.getInstance()
						.apply { item["startDate"]?.let { timeInMillis = it.toString().toLong() } }
					color = item["color"].toString().toInt()
					amount = item["amount"].toString().toFloat()
					wholeDay = item["wholeDay"].toString().toBoolean()
					prize = (item["prize"] as? HashMap<*, *>)?.let { Prize(Prize.Type.valueOf(it["type"].toString()), it["amount"].toString().toFloat()) }
					tags = (item["tags"] as? ArrayList<*>)?.map { it.toString().toInt() }?.toIntArray()
					done = item["done"].toString().toBoolean()
					settlements = (item["settlements"] as? ArrayList<*>)?.map { Gson().fromJson(it.toString(), Calendar::class.java) }?.toMutableList()
				}
			}
		}
	}
}