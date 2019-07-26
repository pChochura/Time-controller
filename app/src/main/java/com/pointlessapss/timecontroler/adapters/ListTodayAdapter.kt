package com.pointlessapss.timecontroler.adapters

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.utils.Utils

class ListTodayAdapter(private val items: MutableList<Item>) :
	RecyclerView.Adapter<ListTodayAdapter.DataObjectHolder>() {

	lateinit var context: Context
	lateinit var clickListener: ClickListener

	init {
		setHasStableIds(true)
	}

	override fun getItemId(position: Int): Long {
		return if (position < items.size) items[position].id.toLong() else 0L
	}

	inner class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val iconImage: AppCompatImageView = itemView.findViewById(R.id.iconImage)
		val iconText: AppCompatTextView = itemView.findViewById(R.id.iconText)
		val buttonConfigure: AppCompatImageView = itemView.findViewById(R.id.buttonConfigure)

		init {
			itemView.findViewById<View>(R.id.card).setOnClickListener {
				clickListener.click(adapterPosition, adapterPosition == items.size)
			}
			buttonConfigure.setOnClickListener {
				clickListener.clickConfigure(adapterPosition)
			}
		}
	}

	fun setOnClickListener(clickListener: ClickListener) {
		this.clickListener = clickListener
	}

	@NonNull
	override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): DataObjectHolder {
		context = parent.context
		return DataObjectHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_today, parent, false))
	}

	override fun onBindViewHolder(@NonNull holder: DataObjectHolder, pos: Int) {
		if (pos < items.size) {
			holder.buttonConfigure.visibility = View.VISIBLE

			setColor(holder, items[pos].color)
			holder.iconText.text = items[pos].title.toUpperCase()
		} else {
			holder.buttonConfigure.visibility = View.GONE

			setColor(holder, ContextCompat.getColor(context, R.color.colorTaskDefault))
			holder.iconText.text = "+"
		}

		TextViewCompat.setAutoSizeTextTypeWithDefaults(holder.iconText, TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM)
	}

	private fun setColor(@NonNull holder: DataObjectHolder, @ColorInt color: Int) {
		holder.iconImage.setColorFilter(color)
		holder.iconText.setTextColor(
			if (Utils.getLuminance(color) > 0.5f) {
				ColorUtils.blendARGB(color, Color.BLACK, 0.5f)
			} else {
				color
			}
		)
	}

	override fun getItemCount() = items.size + 1

	interface ClickListener {
		fun clickConfigure(pos: Int)
		fun click(pos: Int, adder: Boolean)
	}
}
