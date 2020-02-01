package com.pointlessapss.timecontroler.views

import android.content.Context
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.adapters.BaseAdapter
import com.pointlessapss.timecontroler.utils.Utils
import org.jetbrains.anko.find
import java.text.SimpleDateFormat
import java.util.*

class MonthPickerView(
	context: Context,
	attrs: AttributeSet?,
	defStyleAttr: Int,
	defStyleRes: Int
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

	private val formatMonth = SimpleDateFormat("MMM", Locale.getDefault())
	private val formatYear = SimpleDateFormat("yyyy", Locale.getDefault())

	val selectedDate: Calendar = Utils.date.apply { set(Calendar.DAY_OF_MONTH, 1) }

	private var size = 0f

	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : this(context, attrs, defStyleAttr, 0)

	constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0, 0)

	init {
		inflate(context, R.layout.view_month_picker, this).post {
			size = width / 4f
			setYearsList()
			handleClicks()
			translateIndicator(selectedDate.get(Calendar.MONTH))
			refreshTitle()
		}
	}

	private fun setYearsList() {
		find<RecyclerView>(R.id.listYears).apply {
			layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
			adapter = ListYearAdapter(Utils.date.let { date ->
				(1950..date.get(Calendar.YEAR)).map { year ->
					formatYear.format(
						date.apply {
							set(
								Calendar.YEAR,
								year
							)
						}.time
					)
				}
			}).apply {
				clickListener = {
					selectedDate.set(Calendar.YEAR, (1950..Utils.date.get(Calendar.YEAR)).toList()[it])
					refreshTitle()
					showMonthPicker()
					notifyDataset()
				}
				scrollToPosition(years.lastIndex)
			}
		}
	}

	private fun handleClicks() {
		for (i in Calendar.JANUARY..Calendar.DECEMBER) {
			find<AppCompatTextView>(resources.getIdentifier("month$i", "id", context.packageName))
				.setOnClickListener {
					translateIndicator(i)
					refreshTitle()
				}
		}
		find<View>(R.id.currentMonth).setOnClickListener {
			showMonthPicker()
		}
		find<View>(R.id.currentYear).setOnClickListener {
			showYearPicker()
		}
	}

	private fun showYearPicker() {
		find<View>(R.id.containerMonths).visibility = View.INVISIBLE
		find<View>(R.id.listYears).visibility = View.VISIBLE
		find<AppCompatTextView>(R.id.currentMonth).setTextColor(ContextCompat.getColor(context, R.color.colorText2))
		find<AppCompatTextView>(R.id.currentYear).setTextColor(ContextCompat.getColor(context, R.color.colorText3))
		TransitionManager.beginDelayedTransition(this, AutoTransition())
	}

	private fun showMonthPicker() {
		find<View>(R.id.containerMonths).visibility = View.VISIBLE
		find<View>(R.id.listYears).visibility = View.GONE
		find<AppCompatTextView>(R.id.currentMonth).setTextColor(ContextCompat.getColor(context, R.color.colorText3))
		find<AppCompatTextView>(R.id.currentYear).setTextColor(ContextCompat.getColor(context, R.color.colorText2))
		TransitionManager.beginDelayedTransition(this, AutoTransition())
	}

	private fun translateIndicator(month: Int) {
		find<View>(R.id.indicator).apply {
			x = (month - (month / 4) * 4) * size
			y = month / 4 * size
		}
		find<AppCompatTextView>(
			resources.getIdentifier(
				"month${selectedDate.get(Calendar.MONTH)}",
				"id",
				context.packageName
			)
		).apply {
			setTextColor(ContextCompat.getColor(context, R.color.colorText1))
		}
		find<AppCompatTextView>(resources.getIdentifier("month$month", "id", context.packageName)).apply {
			setTextColor(ContextCompat.getColor(context, R.color.colorText3))
		}
		selectedDate.set(Calendar.MONTH, month)
	}

	private fun refreshTitle() {
		find<AppCompatTextView>(R.id.currentMonth).text = formatMonth.format(selectedDate.time)
		find<AppCompatTextView>(R.id.currentYear).text = formatYear.format(selectedDate.time)
	}

	inner class ListYearAdapter(val years: List<String>) :
		BaseAdapter<ListYearAdapter.DataObjectHolder>() {

		lateinit var clickListener: (Int) -> Unit

		inner class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
			val text = itemView.find<AppCompatTextView>(R.id.text).apply {
				setOnClickListener {
					clickListener.invoke(adapterPosition)
				}
			}
		}

		override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataObjectHolder {
			return DataObjectHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_year, parent, false))
		}

		override fun onBindViewHolder(holder: DataObjectHolder, position: Int) {
			holder.text.text = years[position]
			holder.text.setTextColor(
				ContextCompat.getColor(
					context, if (selectedDate.get(Calendar.YEAR) == position + 1950)
						R.color.colorText1
					else R.color.colorText2
				)
			)
		}

		override fun getItemCount() = years.size
	}
}