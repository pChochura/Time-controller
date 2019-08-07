package com.pointlessapss.timecontroler.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.pointlessapss.timecontroler.R
import org.jetbrains.anko.find

open class SettingsItem<T>(context: Context) : FrameLayout(context) {

	lateinit var onClickListener: (T) -> Unit

	protected var textTitle: AppCompatTextView? = null
	private var textSubtitle: AppCompatTextView? = null
	protected var image: AppCompatImageView
	private var divider: View? = null
	private var title: String? = null
	private var subtitle: String? = null

	protected open fun getType() = Type.SIMPLE
	protected open fun hasRipple() = true

	init {
		initializeLayout(R.layout.item_settings)

		textTitle = find(R.id.textTitle)
		textSubtitle = find(R.id.textSubtitle)
		image = find(R.id.image)
		divider = find(R.id.divider)

		if (hasRipple()) {
			isClickable = true
			context.obtainStyledAttributes(intArrayOf(android.R.attr.selectableItemBackground)).apply {
				foreground = getDrawable(0)
				recycle()
			}
		}

		if (getType() == Type.WITH_IMAGE) {
			image.visibility = View.VISIBLE
		}
	}

	fun initializeLayout(layout: Int) {
		removeAllViewsInLayout()
		val inflated = LayoutInflater.from(context).inflate(layout, this)
		inflated.layoutParams =
			LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
		inflated.measure(
			MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
			MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
		)
		inflated.layout(0, 0, inflated.measuredWidth, inflated.measuredHeight)
	}

	private fun refreshTitle() {
		textTitle?.text = title
	}

	private fun refreshSubtitle() {
		textSubtitle?.text = subtitle
	}

	fun refresh() {
		refreshTitle()
		refreshSubtitle()
	}

	fun setTitle(title: String) {
		this.title = title
	}

	fun setSubtitle(subtitle: String) {
		this.subtitle = subtitle
	}

	fun hideDivider() {
		divider?.visibility = View.GONE
	}

	fun toggle(enabled: Boolean) {
		super.setEnabled(enabled)
		alpha = if (enabled) 1f else 0.3f
		isClickable = enabled
	}

	enum class Type {
		SIMPLE, WITH_IMAGE
	}
}
