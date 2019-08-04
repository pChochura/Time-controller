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

class ListHistoryAdapter(private val items: MutableList<Item>) :
	RecyclerView.Adapter<ListHistoryAdapter.DataObjectHolder>() {

	lateinit var context: Context
	lateinit var clickListener: ClickListener

	init {
		setHasStableIds(true)
	}

	override fun getItemId(position: Int): Long {
		return items[position].id.toLong()
	}

	inner class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val textTaskName: AppCompatTextView = itemView.findViewById(R.id.textTaskName)
		val textTaskDescription: AppCompatTextView = itemView.findViewById(R.id.textTaskDescription)
		val textFootnote: AppCompatTextView = itemView.findViewById(R.id.textFootnote)

		init {
			itemView.findViewById<View>(R.id.card).setOnClickListener {
				clickListener.click(adapterPosition)
			}
			itemView.findViewById<View>(R.id.buttonRemove).setOnClickListener {
				clickListener.clickRemove(adapterPosition)
			}
		}
	}

	fun setOnClickListener(clickListener: ClickListener) {
		this.clickListener = clickListener
	}

	@NonNull
	override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): DataObjectHolder {
		context = parent.context
		return DataObjectHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false))
	}

	override fun onBindViewHolder(@NonNull holder: DataObjectHolder, pos: Int) {
		setColor(holder, items[pos].color)
		holder.textTaskName.text = items[pos].title
		holder.textTaskDescription.text = Utils.createItemDescription(context, items[pos])
		holder.textFootnote.text = items[pos].getTimeAmount()
		if (items[pos].amount != 0f) {
			holder.textFootnote.visibility = View.VISIBLE
		} else {
			holder.textFootnote.visibility = View.GONE
		}
	}

	private fun setColor(@NonNull holder: DataObjectHolder, @ColorInt color: Int) {
		holder.textTaskName.backgroundTintList = ColorStateList.valueOf(color)
		holder.textTaskName.setTextColor(
			if (Utils.getLuminance(color) > 0.5f) {
				ColorUtils.blendARGB(color, Color.BLACK, 0.5f)
			} else {
				color
			}
		)
	}

	override fun getItemCount() = items.size

	interface ClickListener {
		fun clickRemove(pos: Int)
		fun click(pos: Int)
	}
}
