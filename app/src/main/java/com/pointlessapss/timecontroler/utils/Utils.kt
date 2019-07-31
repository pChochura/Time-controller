package com.pointlessapss.timecontroler.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.text.method.KeyListener
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import androidx.annotation.FloatRange
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import com.pointlessapss.timecontroler.models.Item
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

val Int.dp: Int
	get() = (this * Resources.getSystem().displayMetrics.density).toInt()

object Utils {
	val dateFormat
		get() = SimpleDateFormat("HH:mm, dd MMMM", Locale.getDefault())

	const val UNDEFINED_WINDOW_SIZE = Integer.MAX_VALUE

	@FloatRange(from = 0.0, to = 1.0)
	fun getLuminance(color: Int): Float {
		return (0.299f * Color.red(color) + 0.587f * Color.green(color) + 0.114f * Color.blue(color)) / 255f
	}

	fun makeDialog(activity: Activity, @LayoutRes layout: Int, callback: (Dialog) -> Unit, vararg windowSize: Int) {
		val dialog = Dialog(activity)
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
		dialog.window?.also {
			it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
			it.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
			val layoutParams = dialog.window!!.attributes
			layoutParams.dimAmount = 0.5f
			dialog.window!!.attributes = layoutParams
		}
		val size = getScreenSize(activity)
		val width =
			if (windowSize.isNotEmpty() && windowSize.first() != UNDEFINED_WINDOW_SIZE) windowSize[0]
			else min(350.dp, size.x - 150)
		val height =
			if (windowSize.size > 1 && windowSize[1] != UNDEFINED_WINDOW_SIZE) windowSize[1]
			else min(500.dp, size.y - 150)
		dialog.setContentView(
			LayoutInflater.from(activity).inflate(layout, null),
			ViewGroup.LayoutParams(width, height)
		)
		callback.invoke(dialog)
		if (!dialog.isShowing)
			dialog.show()
	}

	fun getScreenSize(activity: Activity): Point {
		val displayMetrics = DisplayMetrics()
		activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
		return Point(displayMetrics.widthPixels, displayMetrics.heightPixels)
	}

	fun toggleEditText(editText: EditText, enabled: Boolean = false) {
		if (enabled) {
			editText.keyListener = (editText.tag ?: return) as KeyListener
		} else {
			editText.tag = editText.keyListener
			editText.keyListener = null
		}
	}

	fun joinWeekdaysToString(weekdays: BooleanArray): String? {
		if (!weekdays.contains(true)) {
			return null
		}

		val day = Calendar.getInstance()
		day.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

		val format = SimpleDateFormat("EEE", Locale.getDefault())

		val str = StringBuilder()

		weekdays.forEach {
			if (it) {
				str.append(", ")
					.append(format.format(day.time))
			}
			day.add(Calendar.DAY_OF_MONTH, 1)
		}

		return str.substring(2)
	}

	fun createItemDescription(item: Item): String {
		if (item.startDate == null) {
//			TODO get from resources
			return "Whole day"
		}

		val format = SimpleDateFormat("HH:mm", Locale.getDefault())
		val endTime = Calendar.getInstance()
		endTime.timeInMillis = item.startDate!!.timeInMillis
		endTime.add(Calendar.HOUR_OF_DAY, item.amount.toInt())
		endTime.add(Calendar.MINUTE, ((item.amount - item.amount.toInt()) * 60).toInt())
		return "${format.format(item.startDate!!.time)} - ${format.format(endTime.time)}"
	}

	fun getColors(context: Context): IntArray {
		val numberOfColors = 16
		val output = IntArray(numberOfColors)
		return output.apply {
			this.forEachIndexed { i, _ ->
				val id = context.resources.getIdentifier("color$i", "color", context.packageName)
				output[i] = ContextCompat.getColor(context, id)
			}
		}
	}
}