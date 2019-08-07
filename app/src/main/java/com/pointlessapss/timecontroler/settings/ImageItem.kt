package com.pointlessapss.timecontroler.settings

import android.content.Context
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat

import com.pointlessapss.timecontroler.R

class ImageItem(context: Context) : SettingsItem<Unit>(context) {

	override fun getType() = Type.WITH_IMAGE

	init {
		setOnClickListener { onClickListener.invoke(Unit) }
	}

	fun setImageColor(@ColorInt color: Int) = image.setColorFilter(color)

	fun setImageResource(@DrawableRes id: Int) = image.apply {
		setImageResource(id)
		setColorFilter(ContextCompat.getColor(context, R.color.colorTransparent))
	}
}
