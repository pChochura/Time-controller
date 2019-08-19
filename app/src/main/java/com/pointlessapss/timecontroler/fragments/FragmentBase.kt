package com.pointlessapss.timecontroler.fragments

import android.animation.Animator
import android.animation.AnimatorInflater
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.LayoutRes
import androidx.core.animation.doOnEnd
import androidx.fragment.app.Fragment
import com.pointlessapss.timecontroler.R

abstract class FragmentBase : Fragment() {

	var rootView: ViewGroup? = null

	var onForceRefreshListener: (() -> Unit)? = null
	var onChangeFragmentListener: ((FragmentBase) -> Unit)? = null
	var onLoadedFragmentListener: (() -> Unit)? = null
	var forceRefresh = false

	@LayoutRes abstract fun getLayoutId(): Int
	abstract fun created()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		if (rootView == null || forceRefresh) {
			forceRefresh = false
			rootView = inflater.inflate(getLayoutId(), container, false) as ViewGroup

			created()
		}
		return rootView
	}

	@SuppressLint("ResourceType")
	override fun onCreateAnimator(transit: Int, enter: Boolean, nextAnim: Int): Animator? {
		return AnimatorInflater.loadAnimator(context!!, if (enter) R.anim.fade_in else R.anim.fade_out).apply {
			doOnEnd {
				onLoadedFragmentListener?.invoke()
			}
		}
	}

	override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
		var anim: Animation? = null
		try {
			anim = AnimationUtils.loadAnimation(context!!, transit).apply { setListener() }
		} catch (e: Exception) {
			try {
				anim = AnimationUtils.loadAnimation(context!!, nextAnim).apply { setListener() }
			} catch (e: Exception) {
			}
		}
		return anim
	}

	private fun Animation.setListener() {
		setAnimationListener(object : Animation.AnimationListener {
			override fun onAnimationRepeat(anim: Animation?) = Unit
			override fun onAnimationStart(anim: Animation?) = Unit
			override fun onAnimationEnd(anim: Animation?) {
				onLoadedFragmentListener?.invoke()
			}
		})
	}
}