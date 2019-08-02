package com.pointlessapss.timecontroler.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
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
	fun toString(array: BooleanArray): String = Gson().toJson(array)

	@JvmStatic
	@TypeConverter
	fun fromList(string: String): BooleanArray = Gson().fromJson(string, BooleanArray::class.java)
}