package com.pointlessapss.timecontroler.settings

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.pointlessapss.timecontroler.utils.dp

class GroupItem(context: Context) : LinearLayout(context) {

	private var header: String? = null
	private var items: Array<out Item>? = null

	fun withVisibility(visible: Boolean): GroupItem {
		this.visibility = if (visible) View.VISIBLE else View.GONE
		return this
	}

	fun withId(id: Int): GroupItem {
		this.id = id
		return this
	}

	fun withHeader(header: String): GroupItem {
		this.header = header
		return this
	}

	fun with(vararg items: Item): GroupItem {
		this.items = items
		return this
	}

	fun build(): LinearLayout {
		return apply {
			orientation = VERTICAL
			layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			header?.also { title ->
				addView(
					HeaderItem(context).apply {
						setTitle(title)
						refresh()
					}
				)
			}
			items?.forEach { item ->
				if (item.imageId != 0) {
					addView(
						ImageItem(context).apply {
							setItem(item)
							setImageColor(item.imageColor)
							setImageResource(item.imageId)
						}
					)
				} else {
					addView(
						SimpleItem(context).apply {
							setItem(item)
						}
					)
				}
			}
		}
	}

	private fun SettingsItem<*>.setItem(item: Item) {
		setTitle(item.title)
		setSubtitle(item.subTitle)
		setOnClickListener { item.clickListener?.invoke(it) }
		if (item.hideDivider) {
			hideDivider()
		}
		if (!item.visibility) {
			visibility = View.GONE
		}
		if (item.id != 0) {
			id = item.id
		}
		toggle(item.toggle)
		alpha = item.alpha
		refresh()
	}

	class Item(
		var title: String = "",
		var subTitle: String = "",
		var clickListener: ((Any) -> Unit)? = null,
		var hideDivider: Boolean = false,
		var toggle: Boolean = true,
		var visibility: Boolean = true,
		var alpha: Float = 1f,
		var id: Int = 0,
		var imageColor: Int = 0,
		var imageId: Int = 0
	)
}