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

class ListPrizePeriodicallyAdapter(private val pair: Pair<Item, MutableList<Item>?>) :
	BaseAdapter<ListPrizePeriodicallyAdapter.DataObjectHolder>() {

	private lateinit var onClickListener: (Int) -> Unit
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

		init {
			itemView.findOptional<View>(R.id.card)?.also {
				it.setOnClickListener {
					onClickListener.invoke(adapterPosition)
				}
			}
		}
	}

	fun setOnClickListener(onClickListener: (Int) -> Unit) {
		this.onClickListener = onClickListener
	}

	override fun getItemViewType(position: Int): Int {
		return if (position == itemCount - 1) 0 else 1
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
				if (viewType == 0) R.layout.item_add
				else R.layout.item_prize_periodically,
				parent,
				false
			)
		)
	}

	override fun onBindViewHolder(@NonNull holder: DataObjectHolder, pos: Int) {
		holder.textPeriod?.text = Utils.formatDate.format(pair.first.settlements!![pos].time)
		holder.textPrize?.text = map[pos].toString()
	}

	override fun getItemCount() = (pair.first.settlements?.size ?: 0) + 1
}
