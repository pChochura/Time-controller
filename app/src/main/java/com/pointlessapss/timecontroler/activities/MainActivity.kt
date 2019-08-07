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
		const val ANALYTICS = 0
		const val HOME = 1
		const val SETTINGS = 2
	}

	private lateinit var db: AppDatabase

	private var currentFragment: Fragment? = null
	private var fragments = arrayOfNulls<FragmentBase>(3)
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
				R.id.settings-> {
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
		}
		fragments[HOME] = FragmentHome().apply {
			setDb(db)
			setOnMonthChangeListener {
				val text = Utils.formatMonthLong.format(it.time)
				supportActionBar?.title = text
			}
		}
		fragments[SETTINGS] = FragmentSettings().apply {
			setDb(db)
			setOnForceRefreshListener {
				fragments.forEach { it?.forceRefresh = true }
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