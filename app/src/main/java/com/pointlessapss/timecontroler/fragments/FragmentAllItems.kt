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
import org.jetbrains.anko.uiThread

class FragmentAllItems : FragmentBase() {

	private lateinit var db: AppDatabase
	private lateinit var items: MutableList<Item>
	private lateinit var itemsDone: MutableList<Item>

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
			adapter = ListHistoryAdapter(items.map { it to it }, true).also { adapter ->
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
				val parent = items.removeAt(pos)
				val toRemove = itemsDone.filter { it.parentId == parent.id }
				db.itemDao().deleteAll(*(toRemove.toTypedArray()))
				db.itemDao().deleteAll(parent)
				uiThread {
					listHistoryAdapter.notifyDataSetChanged()
					onForceRefreshListener?.invoke()
				}
			}
		}
	}

	private fun clickEdit(pos: Int, adapter: ListHistoryAdapter) {
		FragmentAddTask(items.getOrNull(pos)).apply {
			setSaveListener { item ->
				if (adapter.getItemViewType(pos) == 1) {
					items[pos] = item
				} else {
					items.add(item)
				}
				doAsync {
					db.itemDao().insertAll(item)
					uiThread {
						adapter.notifyDataSetChanged()
						onForceRefreshListener?.invoke()
					}
				}
			}
		}.show(childFragmentManager, "editTaskFragment")
	}

	fun setTasksCreated(items: MutableList<Item>) {
		this.items = items
	}

	fun setTasksDone(itemsDone: MutableList<Item>) {
		this.itemsDone = itemsDone
	}
}