package com.pointlessapss.timecontroler.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.app.progresviews.ProgressLine
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.models.ItemType
import com.pointlessapss.timecontroler.utils.Utils

class ListHistoryAdapter(private val items: MutableList<Item>) :
	RecyclerView.Adapter<ListHistoryAdapter.DataObjectHolder>() {

	private var context: Context? = null

	init {
		setHasStableIds(true)
	}

	override fun getItemId(position: Int): Long {
		return items[position].id.toLong()
	}

	inner class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val title: AppCompatTextView = itemView.findViewById(R.id.title)
		val subtitle: AppCompatTextView = itemView.findViewById(R.id.subtitle)
		val footnote: AppCompatTextView = itemView.findViewById(R.id.footnote)
		val progress: ProgressLine = itemView.findViewById(R.id.progress)
	}

	@NonNull
	override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): DataObjectHolder {
		context = parent.context
		return DataObjectHolder(LayoutInflater.from(context).inflate(R.layout.item_history, parent, false))
	}

	override fun onBindViewHolder(@NonNull holder: DataObjectHolder, pos: Int) {
		holder.title.text = items[pos].title
		holder.subtitle.text = Utils.dateFormat.format(items[pos].startDate.time)
		if (items[pos].type == ItemType.TimeBased) {
			holder.footnote.text = items[pos].defaultTimeAmount
			holder.progress.setmValueText(items[pos].getTimeAmount())
		} else {
			holder.progress.setmDefText(context!!.resources.getQuantityString(R.plurals.times, items[pos].amount.toInt()))
			holder.progress.setmValueText(items[pos].amount.toInt())
		}
		holder.progress.setmPercentage(items[pos].getPercentage())
	}

	override fun getItemCount() = items.size
}
