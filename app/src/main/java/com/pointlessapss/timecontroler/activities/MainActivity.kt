package com.pointlessapss.timecontroler.activities

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.sundeepk.compactcalendarview.CompactCalendarView
import com.google.gson.Gson
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.adapters.ListTodayAdapter
import com.pointlessapss.timecontroler.fragments.FragmentAddTask
import com.pointlessapss.timecontroler.fragments.FragmentOptions
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.utils.DialogUtil
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
		tasksToday.addAll(tasksCreated.filter { it.defaultWeekdays[currentDay - 1] }.sortedBy { it.startDate })
	}

	private fun setTodayList() {
		listTodayAdapter = ListTodayAdapter(tasksToday)
		listTodayAdapter.setOnClickListener(object : ListTodayAdapter.ClickListener {
			override fun clickConfigure(pos: Int) {
				onTaskEditClick(tasksToday[pos])
			}

			override fun click(pos: Int, adder: Boolean) {
				if (adder) {
					onTaskAddClick()
					return
				}

				onTaskClick(tasksToday[pos])
			}
		})
		listToday.layoutManager = LinearLayoutManager(applicationContext, RecyclerView.HORIZONTAL, false)
		listToday.adapter = listTodayAdapter
	}

	private fun setCalendar() {
		calendar.setOnMonthChangeListener {
			val text = SimpleDateFormat("MMMM", Locale.getDefault()).format(it.time)
			Log.d("LOG!", "changed to: $text")
			supportActionBar?.title = text
		}
//		onMonthScroll(Calendar.getInstance().time)
	}

	private fun showInfoItemDialog(item: Item, callbackOk: () -> Unit, toEdit: Boolean = false) {
		val dialogItemInfo = object : DialogUtil.StatefulDialog() {
			override fun toggle() {
				toggled = !toggled

				val textTitle = dialog.findViewById<AppCompatEditText>(R.id.textTitle)
				val rootView = dialog.window!!.decorView.rootView as ViewGroup

				if (toggled) {
					dialog.findViewById<View>(R.id.buttonEdit).visibility = View.GONE
					dialog.findViewById<View>(R.id.containerInfo).visibility = View.GONE
					dialog.findViewById<View>(R.id.containerEdit).visibility = View.VISIBLE
				} else {
					dialog.findViewById<View>(R.id.buttonEdit).visibility = View.VISIBLE
					dialog.findViewById<View>(R.id.containerInfo).visibility = View.VISIBLE
					dialog.findViewById<View>(R.id.containerEdit).visibility = View.GONE
				}

				Utils.toggleEditText(textTitle, toggled)
				TransitionManager.beginDelayedTransition(rootView, AutoTransition())
			}
		}

		DialogUtil.create(dialogItemInfo, this,
			R.layout.dialog_item_info, { statefulDialog ->
				val dialog = statefulDialog.dialog
				val rootView = dialog.window!!.decorView.rootView as ViewGroup

				FragmentOptions.handleOptions(
					this, rootView, item, arrayOf(
						R.id.optionWeekdays,
						R.id.optionColor
					)
				)

				val textTitle = dialog.findViewById<AppCompatEditText>(R.id.textTitle)
				textTitle.setText(item.title)
				dialog.findViewById<AppCompatTextView>(R.id.textContent).text = Utils.createItemDescription(this, item)

				dialog.findViewById<View>(R.id.buttonOk).setOnClickListener {
					dialog.dismiss()
					callbackOk.invoke()
				}

				dialog.findViewById<View>(R.id.buttonEdit).setOnClickListener {
					statefulDialog.toggle()
				}

				if (toEdit) {
					statefulDialog.showToggled = true
				} else {
					Utils.toggleEditText(textTitle)
				}
			}, Utils.UNDEFINED_WINDOW_SIZE, ViewGroup.LayoutParams.WRAP_CONTENT
		)
	}

	private fun onTaskEditClick(item: Item) {
		val setItem = Item()
		setItem.set(item, Calendar.getInstance())
		showInfoItemDialog(setItem, {
			tasksDone.add(setItem)

//			calendar.addEvent(Event(setItem.color, setItem.startDate!!.timeInMillis), true)
		}, true)
	}

	private fun onTaskClick(item: Item) {
		val setItem = Item()
		setItem.set(item, Calendar.getInstance())
		showInfoItemDialog(setItem, {
			tasksDone.add(setItem)

//			calendar.addEvent(Event(setItem.color, setItem.startDate!!.timeInMillis), true)
		})
	}

	private fun onTaskAddClick() {
		FragmentAddTask().apply {
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