package com.pointlessapss.timecontroler.fragments

import android.os.Handler
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kizitonwose.calendarview.CalendarView
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.CalendarMonth
import com.kizitonwose.calendarview.ui.DayBinder
import com.kizitonwose.calendarview.ui.MonthHeaderFooterBinder
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.adapters.ListHistoryAdapter
import com.pointlessapss.timecontroler.adapters.ListTodayAdapter
import com.pointlessapss.timecontroler.containers.DayViewContainer
import com.pointlessapss.timecontroler.containers.MonthHeaderContainer
import com.pointlessapss.timecontroler.database.AppDatabase
import com.pointlessapss.timecontroler.models.Event
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.utils.DialogUtil
import com.pointlessapss.timecontroler.utils.Utils
import com.pointlessapss.timecontroler.utils.dp
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.uiThread
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

class FragmentHome : FragmentBase() {

	private var selectedDay = Calendar.getInstance()
	private var events = mutableListOf<Event>()
	private var tasksCreated = mutableListOf<Item>()
	private var tasksDone = mutableListOf<Item>()
	private var tasksToday = mutableListOf<Item>()
	private var tasksHistory = mutableListOf<Pair<Item, Item>>()
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
		loadData {
			setTodayList()
			setHistoryList()
			setCalendar()
		}
	}

	fun setDb(db: AppDatabase) {
		this.db = db
	}

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
				setTasksCreated(tasksCreated)
				setTasksDone(tasksDone)
			})
		}
	}

	private fun loadData(callback: () -> Unit) {
		doAsync {
			tasksCreated.clear()
			tasksCreated.addAll(db.itemDao().getAll().toMutableList())
			generateTodayTasks()

			tasksDone.clear()
			tasksDone.addAll(db.itemDao().getAll(true).toMutableList())

			events.clear()
			events.addAll(tasksDone.map { task ->
				Event(
					task,
					color = tasksCreated.find { it.id == task.parentId }!!.color
				)
			})

			uiThread {
				callback.invoke()
			}
		}
	}

	private fun insertItemsDone(vararg item: Item) {
		doAsync {
			db.itemDao().insertAllDone(*item)
			onForceRefreshListener?.invoke()
		}
	}

	private fun insertItemsCreated(vararg item: Item) {
		doAsync {
			db.itemDao().insertAll(*item)
			onForceRefreshListener?.invoke()
		}
	}

	private fun deleteItemsDone(vararg item: Item) {
		doAsync {
			db.itemDao().deleteAll(*item)
			onForceRefreshListener?.invoke()
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
		listToday.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
		listToday.adapter = listTodayAdapter
	}

	private fun setHistoryList() {
		listHistoryAdapter = ListHistoryAdapter(tasksHistory)
		listHistoryAdapter.setOnClickListener(object : ListHistoryAdapter.ClickListener {
			override fun clickRemove(pos: Int) {
				DialogUtil.showMessage(
					activity!!,
					resources.getString(R.string.want_to_remove),
					true
				) {
					val item = tasksHistory.removeAt(pos)
					tasksDone.remove(item.second)
					events.removeAll { it.id == item.second.id }
					calendar.notifyCalendarChanged()
					deleteItemsDone(item.second)
					Handler().post {
						refreshListHistory()
					}
				}
			}

			override fun click(pos: Int) {
				showTaskPreview(tasksHistory[pos].second, true) { item ->
					tasksDone[tasksDone.indexOfFirst { it.id == tasksHistory[pos].second.id }] =
						item
					events.find { it.id == tasksHistory[pos].second.id }?.apply {
						color = item.color
						date = item.startDate!!
					}
					calendar.notifyCalendarChanged()
					showDayHistory(selectedDay)

					insertItemsDone(item)
				}
			}
		})
		listHistory.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
		listHistory.adapter = listHistoryAdapter
	}

	private fun setCalendar() {
		calendar.dayBinder = object : DayBinder<DayViewContainer> {
			override fun create(view: View) = DayViewContainer(view).apply {
				onClickListener = { day ->
					selectedDay = GregorianCalendar(
						day.date.year,
						day.date.monthValue - 1,
						day.date.dayOfMonth
					)
					calendar.notifyCalendarChanged()
					showDayHistory(selectedDay)
				}
			}

			override fun bind(container: DayViewContainer, day: CalendarDay) {
				container.bind(day, selectedDay)
				container.addEvents(requireContext(), events.filter { e ->
					e.date[Calendar.YEAR] == day.date.year &&
							e.date[Calendar.MONTH] == day.date.monthValue - 1 &&
							e.date[Calendar.DAY_OF_MONTH] == day.date.dayOfMonth
				})
			}
		}
		calendar.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthHeaderContainer> {
			override fun create(view: View) = MonthHeaderContainer(view)
			override fun bind(container: MonthHeaderContainer, month: CalendarMonth) {
				container.text.text = month.yearMonth.format(DateTimeFormatter.ofPattern("MMMM"))
			}
		}
		val currentMonth = YearMonth.now()
		val firstMonth = tasksDone.minBy { it.startDate!! }?.startDate?.let {
			YearMonth.of(it.get(Calendar.YEAR), it.get(Calendar.MONTH))
		} ?: YearMonth.now()
		val lastMonth = currentMonth.plusMonths(6)
		calendar.setup(firstMonth, lastMonth, DayOfWeek.MONDAY)
		calendar.scrollToMonth(currentMonth)
		calendar.dayHeight = 50.dp

		showDayHistory(selectedDay)
	}

	private fun showDayHistory(day: Calendar) {
		tasksHistory.clear()
		tasksHistory.addAll(tasksDone.filter {
			it.startDate?.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR)
					&& it.startDate?.get(Calendar.YEAR) == day.get(Calendar.YEAR)
		}.map { item -> tasksCreated.find { it.id == item.parentId }!! to item })
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

				val rootView = dialog.window!!.decorView.rootView as ViewGroup

				if (toggled) {
					dialog.find<View>(R.id.buttonEdit).visibility = View.GONE
					dialog.find<View>(R.id.containerInfo).visibility = View.GONE
					dialog.find<View>(R.id.containerEdit).visibility = View.VISIBLE
				} else {
					dialog.find<View>(R.id.buttonEdit).visibility = View.VISIBLE
					dialog.find<View>(R.id.containerInfo).visibility = View.VISIBLE
					dialog.find<View>(R.id.containerEdit).visibility = View.GONE
				}

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

				dialog.find<AppCompatTextView>(R.id.textTitle).text = item.title
				dialog.find<AppCompatTextView>(R.id.textContent).text =
					Utils.createItemDescription(requireContext(), item)

				dialog.find<View>(R.id.buttonOk).setOnClickListener {
					dialog.dismiss()
					callbackOk.invoke()
				}

				dialog.find<View>(R.id.buttonEdit).setOnClickListener {
					statefulDialog.toggle()
				}

				if (toEdit) {
					statefulDialog.showToggled = true
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

	private fun showTaskPreview(
		item: Item,
		editable: Boolean = false,
		callback: ((Item) -> Unit)? = null
	) {
		val setItem = Item()
		setItem.setParent(item, selectedDay.apply {
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
			tasksDone.add(setItem)
			events.add(
				Event(
					setItem,
					color = tasksCreated.find { it.id == setItem.parentId }!!.color
				)
			)
			calendar.notifyDateChanged(setItem.startDate?.let {
				LocalDate.of(
					it.get(Calendar.YEAR),
					it.get(Calendar.MONTH) + 1,
					it.get(Calendar.DAY_OF_MONTH)
				)
			}!!)
			showDayHistory(selectedDay)

			insertItemsDone(setItem)
		}, editable)
	}

	private fun onTaskAddClick() {
		FragmentAddTask().apply {
			setSaveListener { item ->
				tasksCreated.add(item)
				generateTodayTasks()
				listTodayAdapter.notifyDataSetChanged()

				insertItemsCreated(item)
			}
		}.show(childFragmentManager, "addTaskFragment")
	}
}