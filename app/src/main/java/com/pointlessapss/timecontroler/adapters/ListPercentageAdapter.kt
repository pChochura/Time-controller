package com.pointlessapss.timecontroler.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.utils.Utils
import com.pointlessapss.timecontroler.views.ProgressLine

class ListPercentageAdapter(var items: List<Pair<Item?, List<Float>?>>) :
	BaseAdapter<ListPercentageAdapter.DataObjectHolder>() {

	lateinit var context: Context
	lateinit var clickListener: (Int) -> Unit

	private val average = items.map { it.first to it.second!!.sum() / (it.second!!.size * it.first!!.amount) }

	init {
		setHasStableIds(true)
	}

	override fun getItemId(position: Int): Long {
		return position.toLong()
	}

	inner class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val textTaskName: AppCompatTextView = itemView.findViewById(R.id.textTaskName)
		val progress: ProgressLine = itemView.findViewById(R.id.progress)

		init {
			itemView.findViewById<View>(R.id.card)?.setOnClickListener {
				clickListener.invoke(adapterPosition)
			}
		}
	}

	fun setOnClickListener(clickListener: (Int) -> Unit) {
		this.clickListener = clickListener
	}

	@NonNull
	override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): DataObjectHolder {
		context = parent.context
		return DataObjectHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_percentage, parent, false))
	}

	override fun onBindViewHolder(@NonNull holder: DataObjectHolder, pos: Int) {
		items[pos].first?.let { item ->
			holder.textTaskName.text = item.title
			holder.progress.setProgress(average[pos].second)
			holder.progress.setValue((average[pos].second * 100).toInt().toString() + "%")
			holder.progress.setLabel(item.getTimeAmount(average[pos].second * item.amount))
			setColor(holder, item.color)
		}
	}

	private fun setColor(@NonNull holder: DataObjectHolder, @ColorInt color: Int) {
		holder.progress.setProgressColor(color)
		holder.textTaskName.backgroundTintList = ColorStateList.valueOf(color)
		holder.textTaskName.setTextColor(
			if (Utils.getLuminance(color) > 0.5f) {
				ColorUtils.blendARGB(color, Color.BLACK, 0.5f)
			} else {
				ColorUtils.blendARGB(color, Color.WHITE, 0.5f)
			}
		)
	}

	override fun getItemCount() = items.size
}
