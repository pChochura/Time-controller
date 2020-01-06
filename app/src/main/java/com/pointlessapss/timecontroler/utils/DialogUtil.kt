package com.pointlessapss.timecontroler.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import androidx.appcompat.widget.AppCompatTextView
import com.pointlessapss.timecontroler.R
import org.jetbrains.anko.find
import kotlin.math.min

class DialogUtil private constructor(
	private val activity: Context,
	private val id: Int,
	private val windowSize: IntArray
) {

	companion object {
		fun create(activity: Context, id: Int, callback: (Dialog) -> Unit, vararg windowSize: Int) {
			val dialog = DialogUtil(activity, id, windowSize)
			dialog.makeDialog {
				callback.invoke(it)
			}
		}

		fun create(
			statefulDialog: StatefulDialog,
			activity: Context,
			id: Int,
			callback: (StatefulDialog) -> Unit,
			vararg windowSize: Int
		) {
			val dialog = DialogUtil(activity, id, windowSize)
			dialog.makeDialog {
				callback.invoke(statefulDialog.apply { this.dialog = it })
				if (statefulDialog.showToggled) {
					statefulDialog.toggle()
				}
			}
		}

		fun showMessage(
			activity: Context,
			content: String,
			clickable: Boolean = false,
			callback: (() -> Unit)? = null
		) {
			create(activity, R.layout.dialog_message, { dialog ->
				if (clickable) {
					dialog.find<View>(R.id.buttonOk).apply {
						setOnClickListener {
							callback?.invoke()
							dialog.dismiss()
						}
					}.visibility = View.VISIBLE
				}

				dialog.findViewById<AppCompatTextView>(R.id.textContent).text = content

			}, Utils.UNDEFINED_WINDOW_SIZE, ViewGroup.LayoutParams.WRAP_CONTENT)
		}
	}

	fun makeDialog(callback: (Dialog) -> Unit) {
		val dialog = Dialog(activity)
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
		dialog.window?.also {
			it.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
			it.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
			val layoutParams = dialog.window!!.attributes
			layoutParams.dimAmount = 0.5f
			dialog.window!!.attributes = layoutParams
		}
		val size = Utils.getScreenSize()
		val width =
			if (windowSize.isNotEmpty() && windowSize.first() != Utils.UNDEFINED_WINDOW_SIZE) windowSize[0]
			else min(350.dp, size.x - 150)
		val height =
			if (windowSize.size > 1 && windowSize[1] != Utils.UNDEFINED_WINDOW_SIZE) windowSize[1]
			else min(500.dp, size.y - 150)
		dialog.setContentView(
			LayoutInflater.from(activity).inflate(id, null),
			ViewGroup.LayoutParams(width, height)
		)
		callback.invoke(dialog)
		if (!dialog.isShowing)
			dialog.show()
	}

	abstract class StatefulDialog {
		lateinit var dialog: Dialog
		var showToggled = false
		var toggled = false

		abstract fun toggle()
	}
}