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
import com.pointlessapss.timecontroler.views.ProgressLine
import com.pointlessapss.timecontroler.views.ProgressWheel
import org.jetbrains.anko.find

class ListDayCountMonthlyAdapter(private val items: MutableList<Item>) : RecyclerView.Adapter<ListDayCountMonthlyAdapter.DataObjectHolder>() {

	private lateinit var context: Context

	private val map = items.groupingBy { MonthGroup(it) }
		.aggregate { _, acc: MutableList<Item>?, e, first ->
			if (first) {
				mutableListOf(e)
			} else {
				acc?.apply { add(e) }
			}
		}.toList()

	init {
		setHasStableIds(true)
	}

	override fun getItemId(position: Int): Long {
		return position.toLong()
	}

	inner class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val progressLine = itemView.find<ProgressLine>(R.id.progressLine)
	}

	@NonNull
	override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): DataObjectHolder {
		context = parent.context
		return DataObjectHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_day_count_monthly, parent, false))
	}

	override fun onBindViewHolder(@NonNull holder: DataObjectHolder, pos: Int) {
		holder.progressLine.setProgressColor(items.first().color)

		val max = Utils.getMonthWeekdaysCount(map[pos].first.item.weekdays, map[pos].first.calendar)
		val value = map[pos].second!!.size
		holder.progressLine.setValue(value.toString())
		holder.progressLine.setProgress(value.toFloat() / max)
	}

	override fun getItemCount() = map.size
}
