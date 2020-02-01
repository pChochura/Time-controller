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
import org.jetbrains.anko.findOptional

class ListHistoryAdapter(
	private val allItems: List<Pair<Item, Item>>,
	private val withAdder: Boolean = false
) : BaseAdapter<ListHistoryAdapter.DataObjectHolder>() {

	lateinit var context: Context
	lateinit var clickListener: ClickListener

	lateinit var items: Map<Item, MutableList<Item>>

	init {
		setHasStableIds(true)
		prepareItems()
	}

	private fun prepareItems() {
		items = allItems.groupingBy { it.first }.aggregate { _, accumulator, element, first ->
			if (first) {
				return@aggregate mutableListOf(element.second)
			} else {
				return@aggregate accumulator?.apply {
					add(element.second)
				}!!
			}
		}
	}

	override fun notifyDataset() {
		prepareItems()
		super.notifyDataset()
	}

	override fun getItemId(position: Int): Long {
		return position.toLong()
	}

	inner class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
		val textTitle: AppCompatTextView? = itemView.findOptional(R.id.textTitle)
		val iconText: AppCompatTextView? = itemView.findOptional(R.id.iconText)
		val textPeriod: AppCompatTextView? = itemView.findOptional(R.id.textPeriod)
		val textDuration: AppCompatTextView? = itemView.findOptional(R.id.textDuration)

		init {
			itemView.findOptional<View>(R.id.card)?.setOnClickListener {
				clickListener.click(adapterPosition)
			}
			itemView.findOptional<View>(R.id.buttonRemove)?.setOnClickListener {
				clickListener.clickRemove(adapterPosition)
			}
		}
	}

	fun setOnClickListener(clickListener: ClickListener) {
		this.clickListener = clickListener
	}

	override fun getItemViewType(position: Int): Int {
		return if (withAdder && position == items.size) 0 else 1
	}

	@NonNull
	override fun onCreateViewHolder(@NonNull parent: ViewGroup, viewType: Int): DataObjectHolder {
		context = parent.context
		return DataObjectHolder(
			LayoutInflater.from(parent.context).inflate(
				if (viewType == 1) R.layout.item_history else R.layout.item_add,
				parent,
				false
			)
		)
	}

	override fun onBindViewHolder(@NonNull holder: DataObjectHolder, pos: Int) {
		if (getItemViewType(pos) == 1) {
			val parent = items.keys.toList()[pos]
			val list = items.values.toList()[pos].sortedBy { it.startDate }
			val amount = list.sumByDouble { it.amount.toDouble() }.toFloat()
			val description = list.map {
				Utils.createItemDescription(context, it)
			}.joinToString(separator = "\n") { it }

			setColor(holder, parent.color)
			holder.textTitle?.text = parent.title
			holder.iconText?.text = Utils.getInitials(parent.title)
			holder.textPeriod?.text = description
			holder.textDuration?.text = list.first().getTimeAmount(amount)
			if (amount != 0f && list.find { it.wholeDay } ?: false == false) {
				holder.textDuration?.visibility = View.VISIBLE
			} else {
				holder.textDuration?.visibility = View.GONE
			}
		}
	}

	private fun setColor(@NonNull holder: DataObjectHolder, @ColorInt color: Int) {
		holder.iconText?.setTextColor(
			if (Utils.getLuminance(color) > 0.5f) {
				ColorUtils.blendARGB(color, Color.BLACK, 0.5f)
			} else {
				color
			}
		)
		holder.iconText?.backgroundTintList =
			ColorStateList.valueOf(ColorUtils.setAlphaComponent(color, 50))
	}

	override fun getItemCount() = items.size + (if (withAdder) 1 else 0)

	interface ClickListener {
		fun clickRemove(pos: Int)
		fun click(pos: Int)
	}
}
