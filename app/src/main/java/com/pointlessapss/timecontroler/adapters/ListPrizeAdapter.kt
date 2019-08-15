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
import com.pointlessapss.timecontroler.models.MonthGroup
import com.pointlessapss.timecontroler.models.Prize
import com.pointlessapss.timecontroler.utils.Utils
import org.jetbrains.anko.find
import java.util.*

class ListPrizeAdapter(val items: List<Pair<Item, MutableList<Item>?>>) :
	RecyclerView.Adapter<ListPrizeAdapter.DataObjectHolder>() {

	private lateinit var onClickListener: (Int) -> Unit
	private lateinit var context: Context

	private val map = items.map { pair ->
		Prize.getPrizeSum(pair.first.prize!!, (pair.first.settlements?.let { settlements ->
			pair.second?.partition { it.startDate!!.before(settlements.last()) }?.first
		} ?: pair.second)!!)
	}

	init {
		setHasStableIds(true)
	}

	override fun getItemId(position: Int): Long {
		return position.toLong()
	}

	inner class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val textTaskName: AppCompatTextView = itemView.findViewById(R.id.textTaskName)
		val textPrize: AppCompatTextView = itemView.findViewById(R.id.textPrize)

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
		return DataObjectHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_prize, parent, false))
	}

	override fun onBindViewHolder(@NonNull holder: DataObjectHolder, pos: Int) {
		setColor(holder, items[pos].first.color)
		holder.textTaskName.text = items[pos].first.title
		holder.textPrize.text = map[pos].toString()
	}

	private fun setColor(@NonNull holder: DataObjectHolder, @ColorInt color: Int) {
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
