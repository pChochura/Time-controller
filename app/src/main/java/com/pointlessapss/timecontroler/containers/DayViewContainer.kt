package com.pointlessapss.timecontroler.containers

import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.view.ViewGroup
import com.kizitonwose.calendarview.model.CalendarDay
import com.kizitonwose.calendarview.model.DayOwner
import com.kizitonwose.calendarview.ui.ViewContainer
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.models.Event
import com.pointlessapss.timecontroler.utils.Utils
import com.pointlessapss.timecontroler.utils.dp
import kotlinx.android.synthetic.main.calendar_day.view.*
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

class DayViewContainer(view: View) : ViewContainer(view) {

	private val today = Calendar.getInstance()

	var onClickListener: ((CalendarDay) -> Unit)? = null

	fun addEvents(context: Context, events: List<Event>) {
		view.containerEvent.removeAllViews()
		events.forEach { event ->
			view.containerEvent.addView(
				View(context).apply {
					layoutParams = ViewGroup.LayoutParams(8.dp, 8.dp)
					setBackgroundResource(R.drawable.ic_circle)
					backgroundTintList = ColorStateList.valueOf(event.color)
				}
			)
		}
	}

	fun bind(day: CalendarDay, selectedDay: Calendar) {
		view.container.alpha = if (day.owner == DayOwner.THIS_MONTH) 1f else 0.3f
		view.indicatorToday.visibility = if (today[Calendar.YEAR] == day.date.year &&
			today[Calendar.MONTH] == day.date.monthValue - 1 &&
			today[Calendar.DAY_OF_MONTH] == day.date.dayOfMonth
		) View.VISIBLE else View.GONE
		view.indicatorSelected.visibility =
			if (Utils.formatDate.format(selectedDay.time) == day.date.format(
					DateTimeFormatter.ofPattern(
						Utils.formatDate.toPattern()
					)
				)
			) View.VISIBLE else View.GONE
		view.text.text = day.date.dayOfMonth.toString()
		view.setOnClickListener { onClickListener?.invoke(day) }
	}
}