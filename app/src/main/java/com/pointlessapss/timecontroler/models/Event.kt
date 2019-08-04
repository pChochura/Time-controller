package com.pointlessapss.timecontroler.models

import android.graphics.Rect
import java.util.*

class Event(
	item: Item,
	var rect: Rect? = null,
	var date: Calendar = item.startDate!!,
	var id: Int = item.id,
	var color: Int = item.color
)