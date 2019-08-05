package com.pointlessapss.timecontroler.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.models.MonthGroup
import com.pointlessapss.timecontroler.utils.Utils
import com.pointlessapss.timecontroler.views.ProgressWheel

class ListMonthProgressAdapter(private val items: List<Pair<MonthGroup, MutableList<Item>?>>) :
	RecyclerView.Adapter<ListMonthProgressAdapter.DataObjectHolder>() {

	lateinit var context: Context

	init {
		setHasStableIds(true)
	}

	override fun getItemId(position: Int): Long {
		return position.toLong()
	}

	inner class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val textTaskName: AppCompatTextView = itemView.findViewById(R.id.textTaskName)
		val progressWheel: ProgressWheel = itemView.findViewById(R.id.progressWheel)
	}

	@NonNull
	override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): DataObjectHolder {
		context = parent.context
		return DataObjectHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_day_count, parent, false))
	}

	override fun onBindViewHolder(@NonNull holder: DataObjectHolder, pos: Int) {
		holder.textTaskName.text = items[pos].first.title
		items[pos].second?.also { list ->
			holder.progressWheel.apply {
				setProgress(list.size.toFloat() / Utils.getMonthWeekdaysCount(list.first().weekdays, items[pos].first.calendar))
				setValue(list.size.toString())
				setProgressColor(list.first().color)
				setLabel(context.resources.getQuantityString(R.plurals.day, list.size))
			}
		}
	}

	override fun getItemCount() = items.size
}
