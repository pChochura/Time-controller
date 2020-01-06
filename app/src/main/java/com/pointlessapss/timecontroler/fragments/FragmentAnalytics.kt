package com.pointlessapss.timecontroler.fragments

import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.adapters.AnalyticsPageAdapter
import com.pointlessapss.timecontroler.database.AppDatabase
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.utils.addOnTabSelectedListener
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.uiThread

class FragmentAnalytics : FragmentBase() {

	private lateinit var db: AppDatabase
	private lateinit var tasksByParent: Map<Item?, MutableList<Item>?>
	private lateinit var tasksDone: MutableList<Item>
	private lateinit var tasksCreated: List<Item>

	override fun getLayoutId() = R.layout.fragment_analytics

	override fun created() {
		getTasks {
			setTabLayout()
		}
	}

	private fun setTabLayout() {
		val tabLayout = rootView!!.find<TabLayout>(R.id.tabLayout)
		val viewPager = rootView!!.find<ViewPager>(R.id.viewPager)
		tabLayout.also {
			tasksByParent.forEach { parent ->
				it.addTab(it.newTab().apply {
					text = parent.key!!.title
				})
			}

			it.addOnTabSelectedListener { tab ->
				viewPager.setCurrentItem(tab!!.position, true)
			}
		}

		viewPager.apply {
			addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
			adapter = AnalyticsPageAdapter(requireFragmentManager(), tasksByParent).apply {
				onParentChangeListener = {
					db.itemDao().insertAll(it!!)
				}
			}
		}
	}

	private fun getTasks(callback: () -> Unit) {
		doAsync {
			tasksCreated = db.itemDao().getAll()
			tasksDone = db.itemDao().getAll(true).toMutableList()
			tasksByParent = tasksDone
				.groupingBy { it.parentId }
				.aggregate { _, acc: MutableList<Item>?, e, first ->
					if (first) {
						mutableListOf(e)
					} else {
						acc?.apply { add(e) }
					}
				}.mapKeys { entry ->
					tasksCreated.find { it.id == entry.key }
				}
			uiThread {
				callback.invoke()
			}
		}
	}

	fun setDb(db: AppDatabase) {
		this.db = db
	}
}