package com.pointlessapss.timecontroler.fragments

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.adapters.ListHistoryAdapter
import com.pointlessapss.timecontroler.database.AppDatabase
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.utils.DialogUtil
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find

class FragmentAllItems : FragmentBase() {

	private lateinit var db: AppDatabase
	private lateinit var items: MutableList<Item>

	override fun getLayoutId() = R.layout.fragment_all_items

	override fun created() {
		init()
	}

	fun setDb(db: AppDatabase) {
		this.db = db
	}

	private fun init() {
		rootView!!.find<RecyclerView>(R.id.listAll).apply {
			layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
			adapter = ListHistoryAdapter(items, true).also { adapter ->
				adapter.setOnClickListener(object : ListHistoryAdapter.ClickListener {
					override fun clickRemove(pos: Int) {
						clickRemove(pos, adapter)
					}
					override fun click(pos: Int) {
						clickEdit(pos, adapter)
					}
				})
			}
		}
	}

	private fun clickRemove(pos: Int, listHistoryAdapter: ListHistoryAdapter) {
		DialogUtil.showMessage(activity!!, resources.getString(R.string.want_to_remove), true) {
			doAsync {
				db.itemDao().delete(items.removeAt(pos))
				listHistoryAdapter.notifyDataSetChanged()
				onForceRefreshListener?.invoke(this@FragmentAllItems)
			}
		}
	}

	private fun clickEdit(pos: Int, adapter: ListHistoryAdapter) {
		val item = if (adapter.getItemViewType(pos) == 0) Item() else items[pos]
		FragmentAddTask(item).apply {
			setSaveListener { item ->
				if (adapter.getItemViewType(pos) == 1) {
					items[pos] = item
				} else {
					items.add(item)
				}
				doAsync {
					db.itemDao().insertAll(item)
					adapter.notifyDataSetChanged()
					onForceRefreshListener?.invoke(this@FragmentAllItems)
				}
			}
		}.show(childFragmentManager, "editTaskFragment")
	}

	fun setTasks(items: MutableList<Item>) {
		this.items = items
	}
}