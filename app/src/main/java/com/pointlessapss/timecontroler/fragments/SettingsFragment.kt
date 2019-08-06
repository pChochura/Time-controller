package com.pointlessapss.timecontroler.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.pointlessapss.timecontroler.R
import com.pointlessapss.timecontroler.database.AppDatabase
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.find

class SettingsFragment : Fragment() {

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

			displayUserInfo()
			handleClicks()
		}
		return rootView
	}

	private fun handleClicks() {
		rootView!!.apply {
			find<View>(R.id.accountButtonLog).setOnClickListener {
				if (auth?.currentUser == null) {
					startActivityForResult(GoogleSignIn.getClient(activity!!, gso!!).signInIntent, RC_SIGN_IN)
				} else {
					auth?.signOut()
					rootView!!.find<MaterialButton>(R.id.accountName).visibility = View.GONE
					rootView!!.find<MaterialButton>(R.id.accountButtonLog).text = resources.getString(R.string.sign_in)
				}
			}
			find<View>(R.id.synchronizationButton).setOnClickListener {
				val fDB = FirebaseFirestore.getInstance()
				doAsync {
					val list = db.itemDao().getAll().union(db.itemDao().getAll(true))
					fDB.collection("users")
						.document(auth!!.currentUser!!.uid)
						.set(list.map { it.toMap() }.associateBy { it["id"].toString() })
				}
			}
		}
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
		auth?.currentUser?.also {
			rootView!!.find<MaterialButton>(R.id.accountName).apply {
				text = it.displayName
				visibility = View.VISIBLE
			}
			rootView!!.find<MaterialButton>(R.id.accountButtonLog).text =
				resources.getString(R.string.sign_out)
		}
	}

	fun setDb(db: AppDatabase) {
		this.db = db
	}

	companion object {
		const val RC_SIGN_IN = 1
	}
}
