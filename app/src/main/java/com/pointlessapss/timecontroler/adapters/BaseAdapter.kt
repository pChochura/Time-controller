package com.pointlessapss.timecontroler.adapters

import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<T : RecyclerView.ViewHolder> : RecyclerView.Adapter<T>() {
	open fun notifyDataset() = super.notifyDataSetChanged()
}