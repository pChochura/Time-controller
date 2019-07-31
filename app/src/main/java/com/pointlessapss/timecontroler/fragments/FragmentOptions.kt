package com.pointlessapss.timecontroler.fragments

import android.app.Activity
import android.content.res.ColorStateList
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.FrameLayout
import android.widget.TimePicker
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.adapters.ColorsAdapter
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.utils.DialogUtil
import com.pointlessapss.timecontroler.utils.Utils
import java.text.SimpleDateFormat
import java.util.*

class FragmentOptions private constructor(
	private val activity: Activity,
	private val rootView: ViewGroup,
	private val item: Item
) {

	private val weekDayFormat = SimpleDateFormat("EEE", Locale.getDefault())

	companion object {

		fun handleOptions(
			activity: Activity,
			rootView: ViewGroup,
			item: Item,
			optionsToDisable: Array<Int> = arrayOf(R.id.optionDate)
		) {
			val fragment = FragmentOptions(activity, rootView, item)

			fragment.disableOptions(optionsToDisable)

			fragment.refreshOptionWeekdays()
			fragment.refreshOptionStartTime()
			fragment.refreshOptionColor()
			fragment.refreshOptionDuration()
			fragment.refreshOptionDate()

			rootView.findViewById<View>(R.id.optionWeekdays).setOnClickListener {
				fragment.showSelectWeekdaysDialog {
					fragment.refreshOptionWeekdays()
				}
			}
			rootView.findViewById<View>(R.id.optionStartTime).setOnClickListener {
				fragment.showSelectTimeDialog {
					fragment.refreshOptionStartTime()
				}
			}
			rootView.findViewById<View>(R.id.optionColor).setOnClickListener {
				fragment.showSelectColorDialog {
					fragment.refreshOptionColor()
				}
			}
			rootView.findViewById<View>(R.id.optionDuration).setOnClickListener {
				fragment.showSelectDurationDialog {
					fragment.refreshOptionDuration()
				}
			}
			rootView.findViewById<View>(R.id.optionDate).setOnClickListener {
				fragment.showSelectDateDialog {
					fragment.refreshOptionDate()
				}
			}
		}
	}

	private fun disableOptions(optionsToDisable: Array<Int>) {
		optionsToDisable.forEach {
			rootView.findViewById<View>(it).visibility = View.GONE
		}
	}

	private fun refreshOptionWeekdays() {
		val layout = rootView.findViewById<FrameLayout>(R.id.optionWeekdays)
		(layout[1] as AppCompatTextView).text = Utils.joinWeekdaysToString(activity, item.defaultWeekdays)
	}

	private fun refreshOptionStartTime() {
		val layout = rootView.findViewById<FrameLayout>(R.id.optionStartTime)
		if (item.startDate == null) {
			return
		}
		(layout[1] as AppCompatTextView).text =
			SimpleDateFormat("HH:mm", Locale.getDefault()).format(item.startDate!!.time)
	}

	private fun refreshOptionColor() {
		val layout = rootView.findViewById<FrameLayout>(R.id.optionColor)
		if (item.color == 0) {
			item.color = ContextCompat.getColor(activity, R.color.colorTaskDefault)
		}
		(layout[0] as AppCompatImageView).setColorFilter(item.color)
	}

	private fun refreshOptionDuration() {
		val layout = rootView.findViewById<FrameLayout>(R.id.optionDuration)
		if (item.amount == 0f) {
			return
		}
		(layout[1] as AppCompatTextView).text = item.getTimeAmount()
	}

	private fun refreshOptionDate() {
		val layout = rootView.findViewById<FrameLayout>(R.id.optionDate)
		val today = Calendar.getInstance()
		if (item.startDate == null ||
			(item.startDate?.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
					item.startDate?.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
		) {
			(layout[1] as AppCompatTextView).text = activity.resources.getString(R.string.today)
			return
		}
		(layout[1] as AppCompatTextView).text =
			SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(item.startDate!!.time)
	}

	private fun showSelectWeekdaysDialog(callbackOk: () -> Unit) {
		DialogUtil.create(activity, R.layout.dialog_weekday_picker, { dialog ->
			val day = Calendar.getInstance()
			day.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

			val weekdays = item.defaultWeekdays.clone()

			for (i in 1..7) {
				val id = activity.resources.getIdentifier("weekday$i", "id", activity.packageName)

				dialog.findViewById<MaterialButton>(id).apply {
					text = weekDayFormat.format(day.time).toUpperCase()
					backgroundTintList = getSelectedColor(weekdays[i - 1])
				}.setOnClickListener {
					weekdays[i - 1] = !weekdays[i - 1]
					(it as MaterialButton).backgroundTintList = getSelectedColor(weekdays[i - 1])
				}
				day.add(Calendar.DAY_OF_MONTH, 1)
			}

			dialog.findViewById<View>(R.id.buttonOk).setOnClickListener {
				weekdays.copyInto(item.defaultWeekdays)
				dialog.dismiss()
				callbackOk.invoke()
			}
		}, Utils.UNDEFINED_WINDOW_SIZE, ViewGroup.LayoutParams.WRAP_CONTENT)
	}

	private fun showSelectTimeDialog(callbackOk: () -> Unit) {
		val day = Calendar.getInstance()
		val hour = day.get(Calendar.HOUR_OF_DAY)
		val minute = day.get(Calendar.MINUTE)

		DialogUtil.create(activity, R.layout.dialog_time_picker, { dialog ->
			val picker = dialog.findViewById<TimePicker>(R.id.timePicker)
			picker.apply {
				setIs24HourView(true)
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					this.hour = hour
					this.minute = minute
				} else {
					this.currentHour = hour
					this.currentMinute = minute
				}
			}

			dialog.findViewById<View>(R.id.buttonOk).setOnClickListener {
				val date = Calendar.getInstance()
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					date.set(Calendar.HOUR_OF_DAY, picker.hour)
					date.set(Calendar.MINUTE, picker.minute)
				} else {
					date.set(Calendar.HOUR_OF_DAY, picker.currentHour)
					date.set(Calendar.MINUTE, picker.currentMinute)
				}
				item.startDate = date
				dialog.dismiss()
				callbackOk.invoke()
			}

		}, Utils.UNDEFINED_WINDOW_SIZE, ViewGroup.LayoutParams.WRAP_CONTENT)
	}

	private fun showSelectDurationDialog(callbackOk: () -> Unit) {
		DialogUtil.create(activity, R.layout.dialog_time_picker, { dialog ->
			val picker = dialog.findViewById<TimePicker>(R.id.timePicker)
			picker.apply {
				setIs24HourView(true)
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					this.hour = 0
					this.minute = 0
				} else {
					this.currentHour = 0
					this.currentMinute = 0
				}
			}

			dialog.findViewById<View>(R.id.buttonOk).setOnClickListener {
				val date = Calendar.getInstance()
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					date.set(Calendar.HOUR_OF_DAY, picker.hour)
					date.set(Calendar.MINUTE, picker.minute)
				} else {
					date.set(Calendar.HOUR_OF_DAY, picker.currentHour)
					date.set(Calendar.MINUTE, picker.currentMinute)
				}
				item.amount = date.get(Calendar.HOUR_OF_DAY).toFloat() + date.get(Calendar.MINUTE) / 60f
				dialog.dismiss()
				callbackOk.invoke()
			}

		}, Utils.UNDEFINED_WINDOW_SIZE, ViewGroup.LayoutParams.WRAP_CONTENT)
	}

	private fun showSelectDateDialog(callbackOk: () -> Unit) {
		DialogUtil.create(activity, R.layout.dialog_date_picker, { dialog ->
			val picker = dialog.findViewById<DatePicker>(R.id.datePicker)

			if (item.startDate == null) {
				item.startDate = Calendar.getInstance()
			}
			val year = item.startDate!!.get(Calendar.YEAR)
			val month = item.startDate!!.get(Calendar.MONTH)
			val day = item.startDate!!.get(Calendar.DAY_OF_MONTH)
			picker.init(year, month, day) { _, y, m, d ->
				item.startDate?.set(Calendar.YEAR, y)
				item.startDate?.set(Calendar.MONTH, m)
				item.startDate?.set(Calendar.DAY_OF_MONTH, d)
			}

			dialog.findViewById<View>(R.id.buttonOk).setOnClickListener {
				dialog.dismiss()
				callbackOk.invoke()
			}

		}, Utils.UNDEFINED_WINDOW_SIZE, ViewGroup.LayoutParams.WRAP_CONTENT)
	}

	private fun showSelectColorDialog(callbackOk: () -> Unit) {
		DialogUtil.create(activity, R.layout.dialog_color_picker, { dialog ->
			val colors = Utils.getColors(activity)

			val list = dialog.findViewById<RecyclerView>(R.id.numberPicker)
			list.apply {
				layoutManager = GridLayoutManager(context!!, 4, RecyclerView.VERTICAL, false)
				adapter = ColorsAdapter(colors).apply {
					setOnClickListner {
						item.color = colors[it]
						dialog.dismiss()
						callbackOk.invoke()
					}
				}
			}

		}, Utils.UNDEFINED_WINDOW_SIZE, ViewGroup.LayoutParams.WRAP_CONTENT)
	}

	private fun getSelectedColor(checked: Boolean): ColorStateList {
		return ColorStateList.valueOf(
			ContextCompat.getColor(
				activity,
				if (checked)
					R.color.colorTaskDefault
				else R.color.colorTransparent
			)
		)
	}
}