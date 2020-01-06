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
import com.pointlessapss.timecontroler.models.Prize
import com.pointlessapss.timecontroler.utils.Utils
import org.jetbrains.anko.findOptional

class ListPrizePeriodicallyAdapter(private val pair: Pair<Item, List<Item>?>) :
	BaseAdapter<ListPrizePeriodicallyAdapter.DataObjectHolder>() {

	private lateinit var context: Context

	val map = mutableListOf<Float?>()

	init {
		setHasStableIds(true)
		updatePrizes()
	}

	private fun updatePrizes() {
		for (i in 0 until (pair.first.settlements?.size ?: 0)) {
			map.add(
				Prize.getPrizeSum(pair.first.prize, pair.second?.filter {
					(it.startDate?.before(pair.first.settlements!![i]) ?: true) &&
							(i == 0 || it.startDate?.after(pair.first.settlements!![i - 1]) ?: true)
				})
			)
		}
	}

	override fun getItemId(position: Int): Long {
		return position.toLong()
	}

	inner class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val textPeriod: AppCompatTextView? = itemView.findViewById(R.id.textPeriod)
		val textPrize: AppCompatTextView? = itemView.findViewById(R.id.textPrize)
	}

	override fun notifyDataset() {
		map.clear()
		updatePrizes()
		super.notifyDataset()
	}

	@NonNull
	override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): DataObjectHolder {
		context = parent.context
		return DataObjectHolder(
			LayoutInflater.from(parent.context).inflate(
				R.layout.item_prize_periodically,
				parent,
				false
			)
		)
	}

	override fun onBindViewHolder(@NonNull holder: DataObjectHolder, pos: Int) {
		holder.textPeriod?.text = Utils.formatDate.format(pair.first.settlements!![pos].time)
		holder.textPrize?.text = context.getString(R.string.salary, map[pos])
	}

	override fun getItemCount() = pair.first.settlements?.size ?: 0
}
