package com.pointlessapss.timecontroler.models

import android.graphics.Rect
import java.util.*

class Event(item: Item, var rect: Rect? = null) {
	var date = item.startDate!!
	var color = item.color
	var id = item.id
}