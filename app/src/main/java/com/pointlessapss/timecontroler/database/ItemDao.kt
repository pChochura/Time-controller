package com.pointlessapss.timecontroler.database

import androidx.room.*
import com.pointlessapss.timecontroler.models.Item

@Dao
interface ItemDao {

	@Query("SELECT * FROM items WHERE done LIKE :done")
	fun getAll(done: Boolean = false): List<Item>

	@Insert(onConflict = OnConflictStrategy.REPLACE)
	fun insertAll(vararg items: Item)

	@Transaction
	fun insertAllDone(vararg items: Item) {
		insertAll(*items.apply {
			map {
				it.apply {
					done = true
				}
			}
		})
	}

	@Delete
	fun deleteAll(vararg item: Item)
}