@file:Suppress("unused")

package com.pointlessapss.timecontroler.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.pointlessapss.timecontroler.models.Prize
import java.util.*

object Converters {

	@JvmStatic
	@TypeConverter
	fun fromTimestamp(value: Long?): Calendar? = value?.let {
		GregorianCalendar().also { calendar ->
			calendar.timeInMillis = it
		}
	}

	@JvmStatic
	@TypeConverter
	fun toTimestamp(timestamp: Calendar?): Long? = timestamp?.timeInMillis

	@JvmStatic
	@TypeConverter
	fun toString(any: Any?): String = Gson().toJson(any)

	@JvmStatic
	@TypeConverter
	fun toBooleanArray(string: String?): BooleanArray? = Gson().fromJson(string, BooleanArray::class.java)

	@JvmStatic
	@TypeConverter
	fun toIntArray(string: String?): IntArray? = Gson().fromJson(string, IntArray::class.java)

	@JvmStatic
	@TypeConverter
	fun toPrize(string: String?): Prize? = Gson().fromJson(string, Prize::class.java)
}