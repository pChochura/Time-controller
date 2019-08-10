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
import com.google.android.material.textfield.TextInputEditText
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.adapters.ColorsAdapter
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.models.Prize
import com.pointlessapss.timecontroler.utils.DialogUtil
import com.pointlessapss.timecontroler.utils.Utils
import com.savvyapps.togglebuttonlayout.ToggleButtonLayout
import org.jetbrains.anko.find
import java.util.*

class FragmentOptions private constructor(
	private val activity: Activity,
	private val rootView: ViewGroup,
	private val item: Item
) {

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
			fragment.refreshOptionPrize()
			fragment.refreshOptionDate()

			rootView.find<View>(R.id.optionWeekdays).setOnClickListener {
				fragment.showSelectWeekdaysDialog {
					fragment.refreshOptionWeekdays()
				}
			}
			rootView.find<View>(R.id.optionStartTime).setOnClickListener {
				fragment.showSelectTimeDialog {
					fragment.refreshOptionStartTime()
				}
			}
			rootView.find<View>(R.id.optionColor).setOnClickListener {
				fragment.showSelectColorDialog {
					fragment.refreshOptionColor()
				}
			}
			rootView.find<View>(R.id.optionDuration).setOnClickListener {
				fragment.showSelectDurationDialog {
					fragment.refreshOptionDuration()
				}
			}
			rootView.find<View>(R.id.optionPrize).setOnClickListener {
				fragment.showSelectPrizeDialog {
					fragment.refreshOptionPrize()
				}
			}
			rootView.find<View>(R.id.optionTags).setOnClickListener {
				fragment.showSelectTagsDialog {
				}
			}
			rootView.find<View>(R.id.optionDate).setOnClickListener {
				fragment.showSelectDateDialog {
					fragment.refreshOptionDate()
				}
			}
		}
	}

	private fun disableOptions(optionsToDisable: Array<Int>) {
		optionsToDisable.forEach {
			rootView.find<View>(it).visibility = View.GONE
		}
		rootView.find<View>(R.id.optionTags).visibility = View.GONE
	}

	private fun refreshOptionWeekdays() {
		val layout = rootView.find<FrameLayout>(R.id.optionWeekdays)
		(layout[1] as AppCompatTextView).text = Utils.joinWeekdaysToString(activity, item.weekdays)
	}

	private fun refreshOptionStartTime() {
		val layout = rootView.find<FrameLayout>(R.id.optionStartTime)
		(layout[1] as AppCompatTextView).text =
			if (item.startDate == null || item.wholeDay) activity.resources.getString(R.string.start_time)
			else Utils.formatTime.format(item.startDate!!.time)
	}

	private fun refreshOptionColor() {
		val layout = rootView.find<FrameLayout>(R.id.optionColor)
		if (item.color == 0) {
			item.color = ContextCompat.getColor(activity, R.color.colorTaskDefault)
		}
		(layout[0] as AppCompatImageView).setColorFilter(item.color)
	}

	private fun refreshOptionDuration() {
		val layout = rootView.find<FrameLayout>(R.id.optionDuration)
		(layout[1] as AppCompatTextView).text =
			if (item.amount == 0f) activity.resources.getString(R.string.duration)
			else item.getTimeAmount()
	}

	private fun refreshOptionPrize() {
		val layout = rootView.find<FrameLayout>(R.id.optionPrize)
		(layout[1] as AppCompatTextView).text =
			if (item.prize == null) activity.resources.getString(R.string.prize)
			else item.prize?.describe(activity)
	}

	private fun refreshOptionDate() {
		val layout = rootView.find<FrameLayout>(R.id.optionDate)
		val today = Calendar.getInstance()
		if (item.startDate == null ||
			(item.startDate?.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
					item.startDate?.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR))
		) {
			(layout[1] as AppCompatTextView).text = activity.resources.getString(R.string.today)
			return
		}
		(layout[1] as AppCompatTextView).text = Utils.formatDate.format(item.startDate!!.time)
	}

	private fun showSelectWeekdaysDialog(callbackOk: () -> Unit) {
		DialogUtil.create(activity, R.layout.dialog_picker_weekday, { dialog ->
			val day = Calendar.getInstance()
			day.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

			val weekdays = item.weekdays.clone()

			for (i in 1..7) {
				val id = activity.resources.getIdentifier("weekday$i", "id", activity.packageName)

				dialog.find<MaterialButton>(id).apply {
					text = Utils.formatWeekdayShort.format(day.time).toUpperCase()
					backgroundTintList = getSelectedColor(weekdays[i - 1])
				}.setOnClickListener {
					weekdays[i - 1] = !weekdays[i - 1]
					(it as MaterialButton).backgroundTintList = getSelectedColor(weekdays[i - 1])
				}
				day.add(Calendar.DAY_OF_MONTH, 1)
			}

			dialog.find<View>(R.id.buttonOk).setOnClickListener {
				weekdays.copyInto(item.weekdays)
				dialog.dismiss()
				callbackOk.invoke()
			}
		}, Utils.UNDEFINED_WINDOW_SIZE, ViewGroup.LayoutParams.WRAP_CONTENT)
	}

	private fun showSelectTimeDialog(callbackOk: () -> Unit) {
		val time = item.startDate ?: Calendar.getInstance()
		val hour = time.get(Calendar.HOUR_OF_DAY)
		val minute = time.get(Calendar.MINUTE)

		DialogUtil.create(activity, R.layout.dialog_picker_time, { dialog ->
			val picker = dialog.find<TimePicker>(R.id.timePicker)
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

			dialog.find<View>(R.id.buttonRemove).setOnClickListener {
				item.wholeDay = true
				dialog.dismiss()
				callbackOk.invoke()
			}

			dialog.find<View>(R.id.buttonOk).setOnClickListener {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					time.set(Calendar.HOUR_OF_DAY, picker.hour)
					time.set(Calendar.MINUTE, picker.minute)
				} else {
					time.set(Calendar.HOUR_OF_DAY, picker.currentHour)
					time.set(Calendar.MINUTE, picker.currentMinute)
				}
				item.wholeDay = false
				item.startDate = time
				dialog.dismiss()
				callbackOk.invoke()
			}

		}, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
	}

	private fun showSelectDurationDialog(callbackOk: () -> Unit) {
		DialogUtil.create(activity, R.layout.dialog_picker_time, { dialog ->
			val picker = dialog.find<TimePicker>(R.id.timePicker)
			picker.apply {
				setIs24HourView(true)
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					this.hour = item.amount.toInt()
					this.minute = ((item.amount - item.amount.toInt()) * 60).toInt()
				} else {
					this.currentHour = item.amount.toInt()
					this.currentMinute = ((item.amount - item.amount.toInt()) * 60).toInt()
				}
			}

			dialog.find<View>(R.id.buttonRemove).setOnClickListener {
				item.amount = 0f
				dialog.dismiss()
				callbackOk.invoke()
			}

			dialog.find<View>(R.id.buttonOk).setOnClickListener {
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

		}, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
	}

	private fun showSelectDateDialog(callbackOk: () -> Unit) {
		DialogUtil.create(activity, R.layout.dialog_picker_date, { dialog ->
			val picker = dialog.find<DatePicker>(R.id.datePicker)

			val date = (item.startDate ?: Calendar.getInstance()).clone() as Calendar
			val year = date.get(Calendar.YEAR)
			val month = date.get(Calendar.MONTH)
			val day = date.get(Calendar.DAY_OF_MONTH)
			picker.init(year, month, day) { _, y, m, d ->
				date.set(Calendar.YEAR, y)
				date.set(Calendar.MONTH, m)
				date.set(Calendar.DAY_OF_MONTH, d)
			}

			dialog.find<View>(R.id.buttonOk).setOnClickListener {
				item.startDate = date
				dialog.dismiss()
				callbackOk.invoke()
			}

		}, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
	}

	private fun showSelectPrizeDialog(callbackOk: () -> Unit) {
		DialogUtil.create(activity, R.layout.dialog_picker_prize, { dialog ->

			val buttonsType = dialog.find<ToggleButtonLayout>(R.id.buttonsType)
			val textPrize = dialog.find<TextInputEditText>(R.id.textPrize)

			item.prize?.let { prize ->
				textPrize.setText(prize.amount.toString())
				buttonsType.setToggled(prize.type.id, true)
			}

			dialog.find<View>(R.id.buttonRemove).setOnClickListener {
				item.prize = null
				dialog.dismiss()
				callbackOk.invoke()
			}

			dialog.find<View>(R.id.buttonOk).setOnClickListener {
				item.prize = Prize(Prize.Type.fromId(buttonsType.selectedToggles().firstOrNull()?.id ?: 0), textPrize.text.toString().toFloatOrNull() ?: 0f)
				dialog.dismiss()
				callbackOk.invoke()
			}

		}, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
	}

	private fun showSelectTagsDialog(callbackOk: () -> Unit) {
		DialogUtil.create(activity, R.layout.dialog_picker_tags, { dialog ->

			dialog.find<View>(R.id.buttonOk).setOnClickListener {
				dialog.dismiss()
				callbackOk.invoke()
			}

		}, Utils.UNDEFINED_WINDOW_SIZE, ViewGroup.LayoutParams.WRAP_CONTENT)
	}

	private fun showSelectColorDialog(callbackOk: () -> Unit) {
		DialogUtil.create(activity, R.layout.dialog_picker_color, { dialog ->
			val colors = Utils.getColors(activity)

			val list = dialog.find<RecyclerView>(R.id.numberPicker)
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