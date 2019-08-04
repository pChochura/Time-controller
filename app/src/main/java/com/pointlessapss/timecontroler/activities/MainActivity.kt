package com.pointlessapss.timecontroler.activities

import android.os.Bundle
import android.os.Handler
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
import com.pointlessapss.timecontroler.adapters.ListHistoryAdapter
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
	private var tasksHistory = mutableListOf<Item>()
	private lateinit var listTodayAdapter: ListTodayAdapter
	private lateinit var listHistoryAdapter: ListHistoryAdapter
	private lateinit var db: AppDatabase

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		init()
		loadData()
		setTodayList()
		setHistoryList()
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
				calendar.addEvents(tasksDone.map { task -> Event(task) })
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

	private fun deleteItemDone(item: Item) {
		doAsync {
			db.itemDao().delete(item)
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

	private fun setHistoryList() {
		listHistoryAdapter = ListHistoryAdapter(tasksHistory)
		listHistoryAdapter.setOnClickListener(object : ListHistoryAdapter.ClickListener {
			override fun clickRemove(pos: Int) {
				DialogUtil.showMessage(this@MainActivity, resources.getString(R.string.want_to_remove), true) {
					tasksDone.remove(tasksHistory[pos])
					calendar.removeEventById(tasksHistory[pos].id)
					deleteItemDone(tasksHistory[pos])
					tasksHistory.removeAt(pos)
					Handler().post {
						refreshListHistory()
					}
				}
			}

			override fun click(pos: Int) {
				showTaskPreview(tasksHistory[pos], true) { item ->
					tasksDone[tasksDone.indexOfFirst { it.id == tasksHistory[pos].id }] = item
					calendar.setEventById(Event(item, id = tasksHistory[pos].id))
					showDayHistory(calendar.getSelectedDay())

					insertItemDone(item)
				}
			}
		})
		listHistory.layoutManager = LinearLayoutManager(applicationContext, RecyclerView.VERTICAL, false)
		listHistory.adapter = listHistoryAdapter
	}

	private fun setCalendar() {
		calendar.setOnMonthChangeListener {
			val text = Utils.formatMonthLong.format(it.time)
			supportActionBar?.title = text
		}
		calendar.setOnDaySelectedListener { day ->
			showDayHistory(day)
		}
	}

	private fun showDayHistory(day: Calendar) {
		tasksHistory.clear()
		tasksHistory.addAll(tasksDone.filter {
			it.startDate?.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR)
					&& it.startDate?.get(Calendar.YEAR) == day.get(Calendar.YEAR)
		})
		refreshListHistory()
	}

	private fun refreshListHistory() {
		listHistoryAdapter.notifyDataSetChanged()

		if (tasksHistory.isEmpty()) {
			listHistory.visibility = View.GONE
			labelHistory.visibility = View.GONE
		} else {
			listHistory.visibility = View.VISIBLE
			labelHistory.visibility = View.VISIBLE
		}

		TransitionManager.beginDelayedTransition(layout, AutoTransition())
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
					item.title = textTitle.text.toString()
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

	private fun showTaskPreview(item: Item, editable: Boolean = false, callback: ((Item) -> Unit)? = null) {
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
			if (callback != null) {
				callback.invoke(setItem)
				return@showInfoItemDialog
			}
			tasksDone.add(setItem.apply { id = UUID.randomUUID().hashCode() })
			calendar.addEvent(Event(setItem))
			showDayHistory(calendar.getSelectedDay())

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