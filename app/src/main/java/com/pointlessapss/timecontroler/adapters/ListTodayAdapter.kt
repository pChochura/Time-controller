package com.pointlessapss.timecontroler.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.NonNull
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.graphics.ColorUtils
import androidx.core.view.get
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.progresviews.ProgressLine
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.utils.Utils

class ListTodayAdapter(private val items: MutableList<Item>) :
	RecyclerView.Adapter<ListTodayAdapter.DataObjectHolder>() {

	init {
		setHasStableIds(true)
	}

	override fun getItemId(position: Int): Long {
		return items[position].id.toLong()
	}

	inner class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		private val icon = itemView.findViewById<FrameLayout>(R.id.iconContainer)
		val iconImage = icon[0] as AppCompatImageView
		val iconText = icon[1] as AppCompatTextView

		val progress: ProgressLine = itemView.findViewById(R.id.progress)
	}

	@NonNull
	override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): DataObjectHolder {
		return DataObjectHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_today, parent, false))
	}

	override fun onBindViewHolder(@NonNull holder: DataObjectHolder, pos: Int) {
		setColor(holder, items[pos].color)
		holder.iconText.text = items[pos].title.toUpperCase()
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

	override fun getItemCount() = items.size
}
