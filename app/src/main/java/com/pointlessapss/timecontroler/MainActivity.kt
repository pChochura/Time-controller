package com.pointlessapss.timecontroler

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.github.sundeepk.compactcalendarview.domain.Event
import com.google.gson.Gson
import com.pointlessapss.timecontroler.adapters.ListTodayAdapter
import com.pointlessapss.timecontroler.fragments.AddTaskFragment
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.utils.Utils
import kotlinx.android.synthetic.main.activity_main.*
import net.grandcentrix.tray.AppPreferences
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), CompactCalendarView.CompactCalendarViewListener {

	private val tasksCreated = mutableListOf<Item>()
	private val tasksDone = mutableListOf<Item>()
	private val tasksToday = mutableListOf<Item>()
	private lateinit var prefs: AppPreferences
	private lateinit var listTodayAdapter: ListTodayAdapter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		init()
		loadData()
		generateTodayTasks()
		setTodayList()
		setCalendar()
	}

	private fun init() {
		prefs = AppPreferences(applicationContext)
		supportActionBar?.elevation = 0f
		bottomNavigation.selectedItemId = R.id.home
	}

	private fun loadData() {
		val gson = Gson()
		for (i in 0 until prefs.getInt("tasks_created_size", 0)) {
			tasksCreated.add(gson.fromJson(prefs.getString("tasks_created_$i", "{}"), Item::class.java))
		}
	}

	private fun saveItem(item: Item) {
		val n = prefs.getInt("tasks_created_size", 0)
		prefs.put("tasks_created_size", n + 1)
		prefs.put("tasks_created_$n", Gson().toJson(item))
	}

	private fun generateTodayTasks() {
		val day = Calendar.getInstance()
		val currentDay = day.get(Calendar.DAY_OF_WEEK)
		tasksToday.clear()
		tasksToday.addAll(tasksCreated.filter { it.defaultWeekdays[currentDay - 1] })
	}

	private fun setTodayList() {
		listTodayAdapter = ListTodayAdapter(tasksToday)
		listTodayAdapter.setOnClickListener(object : ListTodayAdapter.ClickListener {
			override fun clickConfigure(pos: Int) {
				onTaskEditClick(tasksCreated[pos])
			}

			override fun click(pos: Int, adder: Boolean) {
				if (adder) {
					onTaskAddClick()
					return
				}

				onTaskClick(tasksCreated[pos])
			}
		})
		listToday.layoutManager = LinearLayoutManager(applicationContext, RecyclerView.HORIZONTAL, false)
		listToday.adapter = listTodayAdapter
	}

	private fun setCalendar() {
		calendar.setListener(this)
		onMonthScroll(Calendar.getInstance().time)
	}

	private fun onTaskEditClick(item: Item) {
	}

	private fun onTaskClick(item: Item) {
		val setItem = Item()
		setItem.set(item, Calendar.getInstance())
		showInfoItemDialog(setItem) {
			tasksDone.add(setItem)

			calendar.addEvent(Event(setItem.color, setItem.startDate!!.timeInMillis), true)
		}
	}

	private fun showInfoItemDialog(item: Item, callbackOk: () -> Unit) {
		Utils.makeDialog(this, R.layout.dialog_item_info, { dialog ->

			dialog.findViewById<AppCompatTextView>(R.id.textTitle).text = item.title
			dialog.findViewById<AppCompatTextView>(R.id.textContent).text = Utils.createItemDescription(item)

			dialog.findViewById<View>(R.id.buttonOk).setOnClickListener {
				dialog.dismiss()
				callbackOk.invoke()
			}

			dialog.findViewById<View>(R.id.buttonEdit).setOnClickListener {
				dialog.dismiss()
				onTaskEditClick(item)
			}

		}, Utils.UNDEFINED_WINDOW_SIZE, ViewGroup.LayoutParams.WRAP_CONTENT)
	}

	private fun onTaskAddClick() {
		AddTaskFragment().apply {
			setSaveListener { item ->
				tasksCreated.add(item)
				generateTodayTasks()
				listTodayAdapter.notifyDataSetChanged()

				saveItem(item)
			}
		}.show(supportFragmentManager, "addTaskFragment")
	}

	override fun onDayClick(dateClicked: Date?) {
	}

	override fun onMonthScroll(firstDayOfNewMonth: Date?) {
		supportActionBar?.title = SimpleDateFormat("MMMM", Locale.getDefault()).format(firstDayOfNewMonth!!)
	}
}