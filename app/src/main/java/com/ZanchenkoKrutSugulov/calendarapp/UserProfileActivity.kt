package com.ZanchenkoKrutSugulov.calendarapp

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.ZanchenkoKrutSugulov.calendarapp.dataClasses.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore

@RequiresApi(Build.VERSION_CODES.O)
class UserProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var buttonLogout: Button
    private lateinit var backButton: ImageView
    private lateinit var userEmailView: TextView
    private var currentUser: FirebaseUser? = null

    private lateinit var editUserEmail: EditText
    private lateinit var textPassword: TextView
    private lateinit var editPassword: EditText
    private lateinit var buttonConnectGoogle: Button

    private lateinit var googleSignInClient: GoogleSignInClient
    private val GOOGLE_SIGN_IN = 100


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        auth = FirebaseAuth.getInstance()
        buttonLogout = findViewById(R.id.logout)
        backButton = findViewById(R.id.backFromUserProfile)
        userEmailView = findViewById(R.id.userEmail)
        currentUser = auth.currentUser
        editUserEmail = findViewById(R.id.editUserEmail)
        textPassword = findViewById(R.id.textPassword)
        editPassword = findViewById(R.id.editPassword)
        buttonConnectGoogle = findViewById(R.id.buttonConnectGoogle)

        loadUserProfile()
        buttonLogout.setOnClickListener {
            auth.signOut()
            startLoginActivity()
        }

        backButton.setOnClickListener {
            finish()
        }


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.defaul_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        buttonConnectGoogle.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
//    signInIntent        startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
        }
    }

    private fun startLoginActivity() {
        startActivity(Intent(this, Login::class.java))
        finish()
    }
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN) {
            val completedTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = completedTask.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            Log.d("UserProfileActivity", "Google Credentials: $credential")
            currentUser?.linkWithCredential(credential)
                ?.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("UserProfileActivity", "linkWithCredential:success")
                        val firebaseUser = task.result?.user
                        updateFirebaseUserProfile(firebaseUser, account)
                    } else {
                        Log.w("UserProfileActivity", "linkWithCredential:failure", task.exception)
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
//    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
////        try {
//            val account = completedTask.getResult(ApiException::class.java)
//            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
//            Log.d("UserProfileActivity", "Google Credentials: $credential")
//            currentUser?.linkWithCredential(credential)
//                ?.addOnCompleteListener(this) { task ->
//                    if (task.isSuccessful) {
//                        Log.d("UserProfileActivity", "linkWithCredential:success")
//                        val firebaseUser = task.result?.user
//                        updateFirebaseUserProfile(firebaseUser, account)
//                    } else {
//                        Log.w("UserProfileActivity", "linkWithCredential:failure", task.exception)
//                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
//                    }
//                }
////        } catch (e: ApiException) {
////            Log.w("UserProfileActivity", "signInResult:failed code=${e.statusCode}")
////        }
//    }


//    private fun handleSignInResult(result: Task<GoogleSignInAccount>) {
//        try {
//            val account = result.getResult(ApiException::class.java)
//            firebaseAuthWithGoogle(account)
//        } catch (e: ApiException) {
//            Log.w("SignInActivity", "Google sign in failed", e)
//        }
//    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                updateFirebaseUserProfile(user, acct)
            } else {
                Log.w("SignInActivity", "Firebase authentication with Google failed", task.exception)
            }
        }
    }


    private fun linkGoogleAccount(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        Log.d("UserProfileActivity", "Google Credentials: " + credential.toString())
        currentUser?.linkWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("UserProfileActivity", "linkWithCredential:success")
                    val firebaseUser = task.result?.user
                    updateFirebaseUserProfile(firebaseUser, account)
                } else {
                    Log.w("UserProfileActivity", "linkWithCredential:failure", task.exception)
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }
    private fun updateFirebaseUserProfile(firebaseUser: FirebaseUser?, googleAccount: GoogleSignInAccount) {
        val userProfileChangeRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(googleAccount.displayName)
            .setPhotoUri(googleAccount.photoUrl)
            .build()

        firebaseUser?.updateProfile(userProfileChangeRequest)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("UserProfileActivity", "User profile updated with Google account info.")
            } else {
                Log.w("UserProfileActivity", "Error updating Firebase user profile", task.exception)
            }
        }

        // Опционально: Обновление пользовательских данных в вашей Firestore или Realtime Database
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(firebaseUser!!.uid)
        Log.d("UserProfileActivity", userRef.toString())
        userRef.update("googleAccountId", googleAccount.id)
            .addOnSuccessListener {
                Log.d("UserProfileActivity", "Google account ID added to Firestore user document.")
            }
            .addOnFailureListener { e ->
                Log.w("UserProfileActivity", "Error adding Google account ID to Firestore", e)
            }
    }

    private fun loadUserProfile() {
        val userId = currentUser!!.uid
        val db = FirebaseFirestore.getInstance()
        val docRef = db.collection("users").document(userId)

        docRef.get().addOnSuccessListener { documentSnapshot ->
            val userProfile = documentSnapshot.toObject(User::class.java)
            userEmailView.text = userProfile?.email
        }.addOnFailureListener { exception ->
            Log.w("UserProfileActivity", "Error getting user details: ", exception)
        }
    }
}