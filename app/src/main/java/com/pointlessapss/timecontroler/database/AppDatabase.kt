package com.pointlessapss.timecontroler.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pointlessapss.timecontroler.converters.Converters
import com.pointlessapss.timecontroler.models.Item

@TypeConverters(Converters::class)
@Database(entities = [Item::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
	abstract fun itemDao(): ItemDao

	companion object {
		@Volatile private var instance: AppDatabase? = null
		private val LOCK = Any()

		operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
			instance ?: buildDatabase(context).also { instance = it }
		}

		private fun buildDatabase(context: Context) = Room.databaseBuilder(
			context,
			AppDatabase::class.java, "database"
		).build()
	}
}