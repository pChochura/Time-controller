package com.pointlessapss.timecontroler

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import com.pointlessapss.timecontroler.adapters.ListTodayAdapter
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.models.ItemType
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), CompactCalendarView.CompactCalendarViewListener {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		setTodayList()
		setCalendar()

		supportActionBar?.elevation = 0f
		bottomNavigation.selectedItemId = R.id.home
	}

	private fun setTodayList() {
		val items = mutableListOf(Item("G2A"), Item("Dominik", ItemType.CountBased))
		val listTodayAdapter = ListTodayAdapter(items)
		listTodayAdapter.setOnClickListener(object : ListTodayAdapter.ClickListener {
			override fun clickConfigure(pos: Int) {
			}

			override fun click(pos: Int, adder: Boolean) {
				if (adder) {

					return
				}

				onTaskClick(items[pos])
			}
		})
		listToday.layoutManager = LinearLayoutManager(applicationContext, RecyclerView.HORIZONTAL, false)
		listToday.adapter = listTodayAdapter
	}

	private fun setCalendar() {
		calendar.setListener(this)
		val date = Calendar.getInstance()
		date.set(Calendar.DAY_OF_MONTH, 12)
		calendar.addEvent(Event(ContextCompat.getColor(applicationContext, R.color.colorTaskDefault), date.timeInMillis))

		onMonthScroll(Calendar.getInstance().time)
	}

	private fun onTaskClick(item: Item) {

	}

	override fun onDayClick(dateClicked: Date?) {
	}

	override fun onMonthScroll(firstDayOfNewMonth: Date?) {
		supportActionBar?.title = SimpleDateFormat("MMMM", Locale.getDefault()).format(firstDayOfNewMonth!!)
	}
}