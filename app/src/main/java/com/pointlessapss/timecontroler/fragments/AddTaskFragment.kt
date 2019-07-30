package com.pointlessapss.timecontroler.fragments

import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TimePicker
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.adapters.ColorsAdapter
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.utils.Utils
import java.text.SimpleDateFormat
import java.util.*

class AddTaskFragment : BottomSheetDialogFragment() {

	private val weekDayFormat = SimpleDateFormat("EEE", Locale.getDefault())
	private var rootView: ViewGroup? = null
	private lateinit var saveListener: (Item) -> Unit

	private val item = Item()

	override fun getTheme() = R.style.AppBottomSheetDialogTheme

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_add_task, container, false) as ViewGroup

			handleClicks()
		}
		return rootView
	}

	fun setSaveListener(saveListener: (Item) -> Unit) {
		this.saveListener = saveListener
	}

	private fun handleClicks() {
		rootView?.findViewById<View>(R.id.buttonSave)?.setOnClickListener {
			val textTaskName = rootView?.findViewById<AppCompatEditText>(R.id.textTaskName)

			item.title = textTaskName?.text.toString()

			if (item.title.isBlank()) {
				showError(resources.getString(R.string.blank_title))
				return@setOnClickListener
			}

			saveListener.invoke(item)

			dismiss()
		}
		rootView?.findViewById<View>(R.id.optionWeekdays)?.setOnClickListener { view ->
			val layout = view as FrameLayout
			showSelectWeekdaysDialog {
				(layout[1] as AppCompatTextView).text = Utils.joinWeekdaysToString(item.defaultWeekdays)
			}
		}
		rootView?.findViewById<View>(R.id.optionStartTime)?.setOnClickListener { view ->
			val layout = view as FrameLayout
			showSelectTimeDialog {
				(layout[1] as AppCompatTextView).text =
					SimpleDateFormat("HH:mm", Locale.getDefault()).format(item.startDate!!.time)
			}
		}
		rootView?.findViewById<View>(R.id.optionColor)?.setOnClickListener { view ->
			val layout = view as FrameLayout
			showSelectColorDialog {
				(layout[0] as AppCompatImageView).setColorFilter(item.color)
			}
		}
		rootView?.findViewById<View>(R.id.optionDuration)?.setOnClickListener { view ->
			val layout = view as FrameLayout
			showSelectDurationDialog {
				(layout[1] as AppCompatTextView).text = item.getTimeAmount()
			}
		}
	}

	private fun showError(content: String) {
		Utils.makeDialog(activity!!, R.layout.dialog_message, { dialog ->

			dialog.findViewById<AppCompatTextView>(R.id.textContent).text = content

		}, Utils.UNDEFINED_WINDOW_SIZE, ViewGroup.LayoutParams.WRAP_CONTENT)
	}

	private fun showSelectWeekdaysDialog(callbackOk: () -> Unit) {
		Utils.makeDialog(activity!!, R.layout.dialog_weekday_picker, { dialog ->
			val day = Calendar.getInstance()
			day.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

			val weekdays = item.defaultWeekdays.clone()

			for (i in 1..7) {
				val id = resources.getIdentifier("weekday$i", "id", context?.packageName)

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

		Utils.makeDialog(activity!!, R.layout.dialog_time_picker, { dialog ->
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
		Utils.makeDialog(activity!!, R.layout.dialog_time_picker, { dialog ->
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
				item.defaultAmount = date.get(Calendar.HOUR_OF_DAY).toFloat() + date.get(Calendar.MINUTE) / 60f
				dialog.dismiss()
				callbackOk.invoke()
			}

		}, Utils.UNDEFINED_WINDOW_SIZE, ViewGroup.LayoutParams.WRAP_CONTENT)
	}

	private fun showSelectColorDialog(callbackOk: () -> Unit) {
		Utils.makeDialog(activity!!, R.layout.dialog_color_picker, { dialog ->
			val colors = Utils.getColors(context!!)

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
				context!!,
				if (checked)
					R.color.colorTaskDefault
				else R.color.colorTransparent
			)
		)
	}
}