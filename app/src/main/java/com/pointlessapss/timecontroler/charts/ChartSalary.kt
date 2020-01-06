package com.pointlessapss.timecontroler.charts

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.adapters.ListPrizePeriodicallyAdapter
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.models.Prize
import com.pointlessapss.timecontroler.utils.DialogUtil
import com.pointlessapss.timecontroler.views.MonthPickerView
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import java.util.*

class ChartSalary(
	context: Context,
	private val parent: Item?,
	private val tasks: List<Item>
) : FrameLayout(context) {

	private lateinit var listPrizeAdapter: ListPrizePeriodicallyAdapter

	var onParentChangeListener: ((Item?) -> Unit)? = null

	constructor(context: Context) : this(context, null, listOf())

	init {
		View.inflate(context, R.layout.chart_salary, this).post {
			find<AppCompatTextView>(R.id.textSalary).text =
				context.getString(R.string.salary, Prize.getPrizeSumSinceLast(parent!!, tasks))

			showSettlements()
		}
	}

	private fun showSettlements() {
		find<RecyclerView>(R.id.listSettlements).apply {
			layoutManager = object : LinearLayoutManager(context!!, RecyclerView.VERTICAL, false) {
				override fun canScrollVertically() = false
			}
			listPrizeAdapter = ListPrizePeriodicallyAdapter(this@ChartSalary.parent!! to tasks)
			adapter = listPrizeAdapter
		}

		find<View>(R.id.buttonAddSettlement).setOnClickListener {
			showPeriodPickerDialog(parent?.prize!!.type) {
				if (parent.settlements == null) {
					parent.settlements = mutableListOf(it)
				} else {
					parent.settlements?.add(it)
				}
				doAsync {
					onParentChangeListener?.invoke(parent)
				}
				listPrizeAdapter.notifyDataset()
			}
		}
	}

	private fun showPeriodPickerDialog(prizeType: Prize.Type, callbackOk: (Calendar) -> Unit) {
		when (prizeType) {
			Prize.Type.PER_MONTH -> {
				DialogUtil.create(context, R.layout.dialog_picker_month, { dialog ->
					dialog.find<View>(R.id.buttonOk).setOnClickListener {
						callbackOk.invoke(dialog.find<MonthPickerView>(R.id.monthPicker).selectedDate)
						dialog.dismiss()
					}
				}, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
			}
			else -> {
				DialogUtil.create(context, R.layout.dialog_picker_date, { dialog ->
					val picker = dialog.find<DatePicker>(R.id.datePicker)

					val date = Calendar.getInstance().apply {
						set(Calendar.SECOND, 0)
						set(Calendar.MINUTE, 0)
						set(Calendar.HOUR_OF_DAY, 0)
					}
					val year = date.get(Calendar.YEAR)
					val month = date.get(Calendar.MONTH)
					val day = date.get(Calendar.DAY_OF_MONTH)
					picker.init(year, month, day) { _, y, m, d ->
						date.set(Calendar.YEAR, y)
						date.set(Calendar.MONTH, m)
						date.set(Calendar.DAY_OF_MONTH, d)
					}
					picker.maxDate = date.timeInMillis

					dialog.find<View>(R.id.buttonOk).setOnClickListener {
						callbackOk.invoke(date)
						dialog.dismiss()
					}
				}, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
			}
		}
	}
}