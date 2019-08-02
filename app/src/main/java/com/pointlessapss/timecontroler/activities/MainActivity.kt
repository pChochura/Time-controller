package com.pointlessapss.timecontroler.activities

import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.adapters.ListTodayAdapter
import com.pointlessapss.timecontroler.database.AppDatabase
import com.pointlessapss.timecontroler.fragments.FragmentAddTask
import com.pointlessapss.timecontroler.fragments.FragmentOptions
import com.pointlessapss.timecontroler.models.Event
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.utils.DialogUtil
import com.pointlessapss.timecontroler.utils.Utils
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*

class MainActivity : AppCompatActivity() {

	private var tasksCreated = mutableListOf<Item>()
	private var tasksDone = mutableListOf<Item>()
	private var tasksToday = mutableListOf<Item>()
	private lateinit var listTodayAdapter: ListTodayAdapter
	private lateinit var db: AppDatabase

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		init()
		loadData()
		setTodayList()
		setCalendar()
	}

	private fun init() {
		supportActionBar?.elevation = 0f
		bottomNavigation.selectedItemId = R.id.home

		db = AppDatabase.invoke(this)
	}

	private fun loadData() {
		doAsync {
			tasksCreated.clear()
			tasksCreated.addAll(db.itemDao().getAll().toMutableList())
			generateTodayTasks()

			tasksDone.clear()
			tasksDone.addAll(db.itemDao().getAll(true).toMutableList())

			uiThread {
				calendar.addEvents(tasksDone.map { task -> Event(task.startDate!!, task.color) })
				listTodayAdapter.notifyDataSetChanged()
			}
		}
	}

	private fun insertItemDone(item: Item) {
		doAsync {
			db.itemDao().insertAllDone(item)
		}
	}

	private fun insertItemCreated(item: Item) {
		doAsync {
			db.itemDao().insertAll(item)
		}
	}

	private fun generateTodayTasks() {
		val day = Calendar.getInstance()
		val currentDay = day.get(Calendar.DAY_OF_WEEK)
		tasksToday.clear()
		tasksToday.addAll(tasksCreated.filter { it.weekdays[currentDay - 1] }.sortedBy { it.startDate })
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
			val text = Utils.formatMonthLong.format(it.time)
			supportActionBar?.title = text
		}
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

		DialogUtil.create(
			dialogItemInfo, this,
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
		showTaskPreview(item, true)
	}

	private fun onTaskClick(item: Item) {
		showTaskPreview(item)
	}

	private fun showTaskPreview(item: Item, editable: Boolean = false) {
		val setItem = Item()
		setItem.set(item, calendar.getSelectedDay().apply {
			firstDayOfWeek = Calendar.MONDAY
			if (item.startDate == null) {
				val day = Calendar.getInstance()
				set(Calendar.HOUR_OF_DAY, day.get(Calendar.HOUR_OF_DAY))
				set(Calendar.MINUTE, day.get(Calendar.MINUTE))
			}
		})
		showInfoItemDialog(setItem, {
			tasksDone.add(setItem)
			calendar.addEvent(Event(setItem.startDate!!, setItem.color))

			insertItemDone(setItem)
		}, editable)
	}

	private fun onTaskAddClick() {
		FragmentAddTask().apply {
			setSaveListener { item ->
				tasksCreated.add(item)
				generateTodayTasks()
				listTodayAdapter.notifyDataSetChanged()

				insertItemCreated(item)
			}
		}.show(supportFragmentManager, "addTaskFragment")
	}
}