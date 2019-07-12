package com.pointlessapss.timecontroler

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pointlessapss.timecontroler.adapters.ListHistoryAdapter
import com.pointlessapss.timecontroler.adapters.ListTodayAdapter
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.models.ItemType
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		setTodayList()
		setHistoryList()
	}

	private fun setTodayList() {
		val items = mutableListOf(Item("G2A"), Item("Dominik"))
		listToday.layoutManager = LinearLayoutManager(applicationContext, RecyclerView.HORIZONTAL, false)
		listToday.adapter = ListTodayAdapter(items)
	}

	private fun setHistoryList() {
		val el1 = Item("G2A")
		el1.startDate = Calendar.getInstance()
		el1.defaultAmount = 8f
		el1.amount = 5f
		el1.type = ItemType.TimeBased

		val el2 = Item("Dominik")
		el2.startDate = Calendar.getInstance()
		el2.defaultAmount = 2f
		el2.amount = 2f
		el2.type = ItemType.CountBased

		listHistory.layoutManager = object : LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false) {
			override fun canScrollVertically() = false
		}
		listHistory.adapter = ListHistoryAdapter(mutableListOf(el1, el2))
	}
}