package com.pointlessapss.timecontroler.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.pointlessapss.timecontroler.R

class ColorsAdapter(private val colors: IntArray) : BaseAdapter<ColorsAdapter.DataObjectHolder>() {

	private lateinit var clickListener: (Int) -> Unit

	inner class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val image: AppCompatImageView = itemView.findViewById(R.id.image)

		init {
			image.setOnClickListener {
				clickListener.invoke(adapterPosition)
			}
		}
	}

	fun setOnClickListener(clickListener: (Int) -> Unit) {
		this.clickListener = clickListener
	}

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataObjectHolder {
		return DataObjectHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_color, parent, false))
	}

	override fun onBindViewHolder(holder: DataObjectHolder, position: Int) {
		holder.image.setColorFilter(colors[position])
	}

	override fun getItemCount() = colors.size
}
