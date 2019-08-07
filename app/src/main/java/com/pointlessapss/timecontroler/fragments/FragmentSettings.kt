package com.pointlessapss.timecontroler.fragments

import android.content.Intent
import android.os.Bundle
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.database.AppDatabase
import com.pointlessapss.timecontroler.models.Item
import com.pointlessapss.timecontroler.settings.GroupItem
import com.pointlessapss.timecontroler.settings.ImageItem
import com.pointlessapss.timecontroler.settings.SimpleItem
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find
import org.jetbrains.anko.findOptional

class FragmentSettings : FragmentBase() {

	private var rootView: ViewGroup? = null

	private lateinit var db: AppDatabase

	private var auth: FirebaseAuth? = null
	private var gso: GoogleSignInOptions? = null

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_settings, container, false) as ViewGroup

			auth = FirebaseAuth.getInstance()
			gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
				.requestIdToken(getString(R.string.default_web_client_id))
				.requestProfile()
				.requestEmail()
				.build()

			init()
			displayUserInfo()
		}
		return rootView
	}

	private fun init() {
		rootView!!.find<ViewGroup>(R.id.bg).apply {
			addView(
				GroupItem(context)
					.withId(ID_ACCOUNT_GROUP)
					.withHeader(resources.getString(R.string.account))
					.with(
						GroupItem.Item(
							subTitle = resources.getString(R.string.google_account_name),
							imageId = R.drawable.ic_google,
							id = ID_ACCOUNT_INFO,
							visibility = false
						),
						GroupItem.Item(
							title = resources.getString(R.string.sign_in),
							subTitle = resources.getString(R.string.tap_to_sign_in),
							clickListener = {
								if (auth?.currentUser == null) {
									startActivityForResult(
										GoogleSignIn.getClient(activity!!, gso!!).signInIntent,
										RC_SIGN_IN
									)
								} else {
									signOut()
								}
							},
							hideDivider = true,
							id = ID_ACCOUNT_BUTTON
						)
					)
					.build()
			)
			addView(
				GroupItem(context)
					.withVisibility(false)
					.withId(ID_SYNC_GROUP)
					.withHeader(resources.getString(R.string.synchronization))
					.with(
						GroupItem.Item(
							title = resources.getString(R.string.upload),
							subTitle = resources.getString(R.string.tap_to_upload),
							clickListener = {
								val fDB = FirebaseFirestore.getInstance()
								doAsync {
									val list = db.itemDao().getAll().union(db.itemDao().getAll(true))
									fDB.collection("users")
										.document(auth!!.currentUser!!.uid)
										.set(list.map { it.toMap() }.associateBy { it["id"].toString() })
								}
							},
							id = ID_SYNC_BUTTON_UPLOAD
						),
						GroupItem.Item(
							title = resources.getString(R.string.download),
							subTitle = resources.getString(R.string.tap_to_download),
							clickListener = {
								val fDB = FirebaseFirestore.getInstance()
								fDB.collection("users")
									.document(auth!!.currentUser!!.uid)
									.get()
									.addOnSuccessListener {
										Item.fromDocument(it)?.let { items ->
											doAsync {
												db.itemDao().insertAll(*items.toTypedArray())
											}
										}
									}
							},
							id = ID_SYNC_BUTTON_DOWNLOAD
						)
					)
					.build()
			)
		}
	}

	private fun signOut() {
		auth?.signOut()
		rootView!!.find<ImageItem>(ID_ACCOUNT_INFO).visibility = View.GONE
		rootView!!.find<SimpleItem>(ID_ACCOUNT_BUTTON).apply {
			setTitle(resources.getString(R.string.sign_in))
			setSubtitle(resources.getString(R.string.tap_to_sign_in))
			refresh()
		}
		rootView!!.find<View>(ID_SYNC_GROUP).visibility = View.GONE
		TransitionManager.beginDelayedTransition(rootView!!.find(R.id.bg), AutoTransition())
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (requestCode == RC_SIGN_IN) {
			val task = GoogleSignIn.getSignedInAccountFromIntent(data)
			try {
				val account = task.getResult(ApiException::class.java)
				auth?.signInWithCredential(GoogleAuthProvider.getCredential(account!!.idToken, null))
					?.addOnSuccessListener { displayUserInfo() }
			} catch (e: ApiException) {
			}
		}
	}

	private fun displayUserInfo() {
		auth?.currentUser?.also { user ->
			rootView!!.find<ImageItem>(ID_ACCOUNT_INFO).apply {
				user.displayName?.let { name -> setTitle(name) }
				visibility = View.VISIBLE
				refresh()
			}

			rootView!!.find<SimpleItem>(ID_ACCOUNT_BUTTON).apply {
				setTitle(resources.getString(R.string.sign_out))
				setSubtitle(resources.getString(R.string.tap_to_sign_out))
				refresh()
			}

			rootView!!.find<View>(ID_SYNC_GROUP).visibility = View.VISIBLE

			TransitionManager.beginDelayedTransition(rootView!!.find(R.id.bg), AutoTransition())
		}
	}

	fun setDb(db: AppDatabase) {
		this.db = db
	}

	companion object {
		const val RC_SIGN_IN = 1

		val ID_ACCOUNT_GROUP = View.generateViewId()
		val ID_ACCOUNT_BUTTON = View.generateViewId()
		val ID_ACCOUNT_INFO = View.generateViewId()

		val ID_SYNC_GROUP = View.generateViewId()
		val ID_SYNC_BUTTON_UPLOAD = View.generateViewId()
		val ID_SYNC_BUTTON_DOWNLOAD = View.generateViewId()
	}
}
