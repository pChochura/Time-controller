package com.pointlessapss.timecontroler.models

import android.content.Context
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.utils.Utils
import com.pointlessapss.timecontroler.utils.round
import java.util.*
import kotlin.math.abs
import kotlin.math.floor

class Prize(var type: Type, var amount: Float) {
	enum class Type(val id: Int) {
		PER_MONTH(R.id.per_month), PER_DAY(R.id.per_day), PER_HOUR(R.id.per_hour),
		PER_TASK(R.id.per_task);

		companion object {
			private fun fromId(id: Int) = values().find { it.id == id } ?: PER_TASK

			fun asText(id: Int, context: Context): String {
				return context.resources.getString(
					context.resources.getIdentifier(
						fromId(id).toString().toLowerCase(),
						"string",
						context.packageName
					)
				)
			}
		}
	}

	fun describe(context: Context) = "${Type.asText(type.id, context)}: $amount"

	companion object {
		fun getPrizeSum(prize: Prize?, list: List<Item>?): Float? {
			return when (prize?.type) {
				Type.PER_MONTH -> {
					val today = Calendar.getInstance()
					list?.filter {
						it.startDate?.get(Calendar.YEAR) != today.get(Calendar.YEAR) ||
								it.startDate?.get(Calendar.MONTH) != today.get(Calendar.MONTH)
					}?.groupBy { MonthGroup(it) }?.size?.times(prize.amount)
				}
				Type.PER_TASK -> {
					list?.size?.times(prize.amount)
				}
				Type.PER_HOUR -> {
					list?.sumByDouble { it.amount.toDouble() }?.toFloat()?.times(prize.amount)
						?.toDouble()?.round()?.toFloat()
				}
				Type.PER_DAY -> {
					list?.groupBy { Utils.formatDate.format(it.startDate!!.time) }?.size?.times(
						prize.amount
					)
				}
				else -> null
			}.let {
				if (abs(it ?: 1f) < 1 && abs(it?.minus(floor(it))?.times(100) ?: 0f) < 1) {
					0f
				} else {
					it
				}
			}
		}

		fun getPrizeSumSinceLast(item: Item, list: List<Item>?): Float? {
			return getPrizeSum(item.prize, item.settlements?.let { settlements ->
				list?.partition { it.startDate!!.before(settlements.last()) }?.second
			} ?: list)
		}
	}
}