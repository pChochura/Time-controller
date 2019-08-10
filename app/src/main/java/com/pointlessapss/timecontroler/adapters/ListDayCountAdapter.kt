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
import org.jetbrains.anko.find
import java.util.*

class ListDayCountAdapter(val items: List<Pair<String, MutableList<Item>?>>) :
	RecyclerView.Adapter<ListDayCountAdapter.DataObjectHolder>() {

	private lateinit var onClickListener: (Int) -> Unit
	private lateinit var context: Context

	private val today = Calendar.getInstance()

	private val map = items.map { pair ->
		var count = 0
		pair.second!!.groupingBy { MonthGroup(it) }
			.aggregate { _, acc: MutableList<Item>?, e, first ->
				if (first) {
					mutableListOf(e)
				} else {
					acc?.apply { add(e) }
				}
			}.keys.forEach { key ->
			count += Utils.getMonthWeekdaysCount(key.item.weekdays, key.calendar, today)
		}
		count
	}

	init {
		setHasStableIds(true)
	}

	override fun getItemId(position: Int): Long {
		return position.toLong()
	}

	inner class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val textTaskName: AppCompatTextView = itemView.findViewById(R.id.textTaskName)
		val progressWheel: ProgressWheel = itemView.findViewById(R.id.progressWheel)

		init {
			itemView.find<View>(R.id.card).setOnClickListener {
				onClickListener.invoke(adapterPosition)
			}
		}
	}

	fun setOnClickListener(onClickListener: (Int) -> Unit) {
		this.onClickListener = onClickListener
	}

	@NonNull
	override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): DataObjectHolder {
		context = parent.context
		return DataObjectHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_day_count, parent, false))
	}

	override fun onBindViewHolder(@NonNull holder: DataObjectHolder, pos: Int) {
		holder.textTaskName.text = items[pos].first
		items[pos].second?.also { list ->
			holder.progressWheel.apply {
				setProgress(list.size.toFloat() / map[pos])
				setValue(list.size.toString())
				setProgressColor(list.first().color)
				setLabel(context.resources.getQuantityString(R.plurals.day, list.size))
			}
		}
	}

	override fun getItemCount() = items.size
}
