package com.pointlessapss.timecontroler.fragments

import android.os.Handler
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.adapters.ListHistoryAdapter
import com.pointlessapss.timecontroler.adapters.ListTodayAdapter
import com.pointlessapss.timecontroler.database.AppDatabase
import com.pointlessapss.timecontroler.models.Event
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.utils.DialogUtil
import com.pointlessapss.timecontroler.utils.Utils
import com.pointlessapss.timecontroler.views.CalendarView
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.uiThread
import java.util.*

class FragmentHome : FragmentBase() {

	private var onMonthChangeListener: ((Calendar) -> Unit)? = null

	private var tasksCreated = mutableListOf<Item>()
	private var tasksDone = mutableListOf<Item>()
	private var tasksToday = mutableListOf<Item>()
	private var tasksHistory = mutableListOf<Item>()
	private lateinit var listTodayAdapter: ListTodayAdapter
	private lateinit var listHistoryAdapter: ListHistoryAdapter
	private lateinit var db: AppDatabase

	private lateinit var calendar: CalendarView
	private lateinit var labelHistory: AppCompatTextView
	private lateinit var listToday: RecyclerView
	private lateinit var listHistory: RecyclerView
	private lateinit var layout: ViewGroup
	private val autoTransition = AutoTransition().apply {
		excludeTarget(R.id.listHistory, true)
		excludeTarget(R.id.calendar, true)
	}

	override fun getLayoutId() = R.layout.fragment_home

	override fun created() {
		init()
		handleClicks()
		loadData()
		setTodayList()
		setHistoryList()
		setCalendar()
	}

	fun setDb(db: AppDatabase) {
		this.db = db
	}

	fun setOnMonthChangeListener(onMonthChangeListener: (Calendar) -> Unit) {
		this.onMonthChangeListener = onMonthChangeListener
	}

	fun getCurrentMonth(): Calendar? = if (::calendar.isInitialized) calendar.getCurrentMonth() else null

	private fun init() {
		calendar = rootView!!.find(R.id.calendar)
		labelHistory = rootView!!.find(R.id.labelHistory)
		listToday = rootView!!.find(R.id.listToday)
		listHistory = rootView!!.find(R.id.listHistory)
		layout = rootView!!.find(R.id.layout)
	}

	private fun handleClicks() {
		rootView!!.find<View>(R.id.buttonShowAll).setOnClickListener {
			onChangeFragmentListener?.invoke(FragmentAllItems().apply {
				setDb(db)
				setTasks(tasksCreated)
			})
		}
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
			}
		}
	}

	private fun insertItemDone(item: Item) {
		doAsync {
			db.itemDao().insertAllDone(item)
			onForceRefreshListener?.invoke(this@FragmentHome)
		}
	}

	private fun insertItemCreated(item: Item) {
		doAsync {
			db.itemDao().insertAll(item)
			onForceRefreshListener?.invoke(this@FragmentHome)
		}
	}

	private fun deleteItemDone(item: Item) {
		doAsync {
			db.itemDao().delete(item)
			onForceRefreshListener?.invoke(this@FragmentHome)
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
		listToday.layoutManager = LinearLayoutManager(context!!, RecyclerView.HORIZONTAL, false)
		listToday.adapter = listTodayAdapter
	}

	private fun setHistoryList() {
		listHistoryAdapter = ListHistoryAdapter(tasksHistory)
		listHistoryAdapter.setOnClickListener(object : ListHistoryAdapter.ClickListener {
			override fun clickRemove(pos: Int) {
				DialogUtil.showMessage(activity!!, resources.getString(R.string.want_to_remove), true) {
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
		listHistory.layoutManager = LinearLayoutManager(context!!, RecyclerView.VERTICAL, false)
		listHistory.adapter = listHistoryAdapter
	}

	private fun setCalendar() {
		calendar.setOnMonthChangeListener {
			onMonthChangeListener?.invoke(it)
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

		TransitionManager.beginDelayedTransition(layout, autoTransition)
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
				TransitionManager.beginDelayedTransition(rootView, autoTransition)
			}
		}

		DialogUtil.create(
			dialogItemInfo, activity!!,
			R.layout.dialog_item_info, { statefulDialog ->
				val dialog = statefulDialog.dialog
				val rootView = dialog.window!!.decorView.rootView as ViewGroup

				FragmentOptions.handleOptions(
					activity!!, rootView, item, arrayOf(
						R.id.optionWeekdays,
						R.id.optionPrize,
						R.id.optionTags,
						R.id.optionColor
					)
				)

				val textTitle = dialog.findViewById<AppCompatEditText>(R.id.textTitle)
				textTitle.setText(item.title)
				dialog.findViewById<AppCompatTextView>(R.id.textContent).text =
					Utils.createItemDescription(context!!, item)

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
		}.show(childFragmentManager, "addTaskFragment")
	}
}