package com.pointlessapss.timecontroler.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.database.AppDatabase
import com.pointlessapss.timecontroler.fragments.FragmentAnalytics
import com.pointlessapss.timecontroler.fragments.FragmentBase
import com.pointlessapss.timecontroler.fragments.FragmentHome
import com.pointlessapss.timecontroler.fragments.FragmentSettings
import com.pointlessapss.timecontroler.utils.Utils
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

	companion object {
		const val ANALYTICS = R.id.analytics
		const val HOME = R.id.home
		const val SETTINGS = R.id.settings
	}

	private lateinit var db: AppDatabase

	private var currentFragment: Fragment? = null
	private var fragments = mutableMapOf<Int, FragmentBase>()
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
					supportActionBar?.title = resources.getString(R.string.analytics)
				}
				R.id.home -> {
					setFragment(HOME)
					(fragments[HOME] as? FragmentHome)?.getCurrentMonth()?.time?.let { time ->
						supportActionBar?.title = Utils.formatMonthLong.format(time)
					}
				}
				R.id.settings -> {
					setFragment(SETTINGS)
					supportActionBar?.title = resources.getString(R.string.settings)
				}
			}
			true
		}

		db = AppDatabase.invoke(this)
	}

	private fun setFragments() {
		fragments[ANALYTICS] = FragmentAnalytics().apply {
			setDb(db)
			onForceRefreshListener = {
				fragments.values.forEach { it.forceRefresh = true }
			}
		}
		fragments[HOME] = FragmentHome().apply {
			setDb(db)
			setOnMonthChangeListener {
				val text = Utils.formatMonthLong.format(it.time)
				supportActionBar?.title = text
			}
			onForceRefreshListener = {
				fragments.values.forEach { it.forceRefresh = true }
			}
			onChangeFragmentListener = {
				setFragment(fragment = it)
				supportActionBar?.title = resources.getString(R.string.all_tasks)
			}
		}
		fragments[SETTINGS] = FragmentSettings().apply {
			setDb(db)
			onForceRefreshListener = {
				fragments.values.forEach { it.forceRefresh = true }
			}
		}
	}

	private fun setFragment(pos: Int = -1, fragment: FragmentBase? = fragments[pos]) {
		if (fragment == currentFragment) {
			return
		}
		fragment?.let {
			val fragmentTransaction = supportFragmentManager.beginTransaction()
			fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
			if (currentFragment != null) fragmentTransaction.replace(R.id.fragmentContainer, fragment).commit()
			else fragmentTransaction.add(R.id.fragmentContainer, fragment).commit()
			currentFragment = fragment
			if (fragments.containsKey(pos)) {
				history.remove(pos)
				history.add(pos)

				bottomNavigation.selectedItemId = pos
			}
		}
	}

	override fun onBackPressed() {
		if (history.size <= 1 && currentFragment === fragments[HOME])
			super.onBackPressed()
		else if (history.size <= 1) {
			setFragment(HOME)
			history.removeAt(history.size - 1)
		} else {
			if (currentFragment === fragments[history[history.size - 1]])
				history.removeAt(history.size - 1)
			setFragment(history[history.size - 1])
		}
	}
}