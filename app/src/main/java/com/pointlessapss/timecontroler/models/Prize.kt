package com.pointlessapss.timecontroler.models

import android.content.Context
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.utils.Utils

class Prize(var type: Type, var amount: Float) {
	enum class Type(val id: Int) {
		PER_MONTH(R.id.per_month), PER_DAY(R.id.per_day), PER_HOUR(R.id.per_hour), PER_TASK(R.id.per_task);

		companion object {
			fun fromId(id: Int) = values().find { it.id == id } ?: PER_TASK

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
		fun getPrizeSum(prize: Prize, list: List<Item>): Float {
			return when (prize.type) {
				Prize.Type.PER_MONTH -> {
					list.groupBy { MonthGroup(it) }.size * prize.amount
				}
				Prize.Type.PER_TASK -> {
					list.size * prize.amount
				}
				Prize.Type.PER_HOUR -> {
					list.sumByDouble { it.amount.toDouble() }.toFloat()
				}
				Prize.Type.PER_DAY -> {
					list.groupBy { Utils.formatDate.format(it.startDate!!.time) }.size * prize.amount
				}
			}
		}
	}
}