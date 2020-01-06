package com.pointlessapss.timecontroler.containers

import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import com.kizitonwose.calendarview.ui.ViewContainer
import kotlinx.android.synthetic.main.calendar_month_header.view.*

class MonthHeaderContainer(view: View) : ViewContainer(view) {
	val text: AppCompatTextView = view.text
}