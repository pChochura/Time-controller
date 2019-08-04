package com.pointlessapss.timecontroler.activities

import android.os.Bundle
import android.os.Handler
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.adapters.ListHistoryAdapter
import com.pointlessapss.timecontroler.adapters.ListTodayAdapter
import com.pointlessapss.timecontroler.database.AppDatabase
import com.pointlessapss.timecontroler.fragments.FragmentAddTask
import com.pointlessapss.timecontroler.fragments.FragmentAnalytics
import com.pointlessapss.timecontroler.fragments.FragmentHome
import com.pointlessapss.timecontroler.fragments.FragmentOptions
import com.pointlessapss.timecontroler.models.Event
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.utils.DialogUtil
import com.pointlessapss.timecontroler.utils.Utils
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*

class MainActivity : AppCompatActivity() {

	companion object {
		const val ANALYTICS = 0
		const val HOME = 1
		const val SETTINGS = 2
	}

	private lateinit var db: AppDatabase

	private var currentFragment: Fragment? = null
	private var fragments = arrayOfNulls<Fragment>(3)
	private var history = mutableListOf<Int>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		init()
		setFragments()
		setFragment(HOME)
	}

	private fun init() {
		supportActionBar?.elevation = 0f
		bottomNavigation.selectedItemId = R.id.home

		bottomNavigation.setOnNavigationItemSelectedListener {
			when (it.itemId) {
				R.id.analytics -> {
					setFragment(ANALYTICS)
				}
				R.id.home -> {
					setFragment(HOME)
				}
				R.id.settings-> {
					setFragment(SETTINGS)
				}
			}
			true
		}

		db = AppDatabase.invoke(this)
	}

	private fun setFragments() {
		fragments[ANALYTICS] = FragmentAnalytics().apply {
			setDb(db)
		}
		fragments[HOME] = FragmentHome().apply {
			setDb(db)
			setOnMonthChangeListener {
				val text = Utils.formatMonthLong.format(it.time)
				supportActionBar?.title = text
			}
		}
	}

	private fun setFragment(pos: Int) {
		history.remove(pos)
		history.add(pos)
		val fragment = fragments[pos]!!
		val fragmentTransaction = supportFragmentManager.beginTransaction()
		fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
		if (currentFragment != null) fragmentTransaction.replace(R.id.fragmentContainer, fragment).commit()
		else fragmentTransaction.add(R.id.fragmentContainer, fragment).commit()
		currentFragment = fragment
	}
}