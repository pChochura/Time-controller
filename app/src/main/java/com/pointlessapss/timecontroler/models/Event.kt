package com.pointlessapss.timecontroler.models

import android.graphics.Rect
import java.util.*

class Event(item: Item, var rect: Rect? = null, var id: Int = item.id) {
	var date = item.startDate!!
	var color = item.color
}