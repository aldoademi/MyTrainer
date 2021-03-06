package com.example.mytrainer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.login.LoginResult
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_login.*
import java.util.*
import com.example.mytrainer.DashboardActivity as DashboardActivity1
import kotlin.toString as toString1

class LoginActivityPT : AppCompatActivity(){

    private lateinit var userArrayList: ArrayList<Atleti>
    lateinit var callbackManager: CallbackManager
    lateinit var mAuth: FirebaseAuth
    private val TAG = "FacebookAuthentication"
    lateinit var facebookBtn: Button
    private lateinit var database: DatabaseReference
    lateinit var ID: String

    //quando l'activity si inizializza, controlla se l'utente è già loggato
    override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser

        var accessToken: AccessToken? = AccessToken.getCurrentAccessToken()

        if(accessToken != null && !accessToken.isExpired){
            updateUIT()
        }

    }

    private fun updateUIT() {
        Toast.makeText(this, " Accesso già effettuato ", Toast.LENGTH_SHORT).show()

        //apre TrainerActivity
        val dashboard = Intent(this, TrainerActivity::class.java)
        startActivity(dashboard)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_pt)

        //inizializza database firebase
        mAuth= Firebase.auth
        callbackManager = CallbackManager.Factory.create();

        login_button.setPermissions("email", "public_profile")
        login_button.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d(TAG, "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
                ID = loginResult.accessToken.userId
            }

            override fun onCancel() {
                Log.d(TAG, "facebook:onCancel")
                // ...
            }

            override fun onError(error: FacebookException) {
                Log.d(TAG, "facebook:onError", error)
                // ...
            }
        })

    }


    //override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    //super.onActivityResult(requestCode, resultCode, data)

    // Pass the activity result back to the Facebook SDK
    //callbackManager.onActivityResult(requestCode, resultCode, data)
    // }

    //se l'utente si è loggato correttamente , riceve un token di accesso, lo scambia per le credenziali di firebase
    //si autentica in firebase con le credenziali.
    private fun handleFacebookAccessToken(accessToken: AccessToken) {
        Log.d(TAG, "HandleFacebookToken $accessToken")

        val credential: AuthCredential = FacebookAuthProvider.getCredential(accessToken.token)
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // login con successo, aggiorna UI con info utente
                    Log.d(TAG, "signInWithCredential:success")
                    val user = mAuth?.currentUser

                    //btn_facebook.setEnabled(true) //visibilità bottone
                    updateUI(user)

                } else {
                    // login fallito, mostra messaggio a utente.
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()

                    //btn_facebook.setEnabled(true)
                    updateUI(null)
                }
            }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
//        Log.d(TAG, "Dentro")
//        Toast.makeText(this, " Login effettuato ", Toast.LENGTH_SHORT).show()
//
//        //apre Dashboard
//        val dashboard = Intent(this, TrainerActivity::class.java).also {
//            startActivity(it)
//
//        }

        userArrayList = arrayListOf()
        var note: String = ""

        var idUtente = mAuth.currentUser?.uid?.toString1()

        val db = FirebaseFirestore.getInstance()
        val dataB = db.collection("Personal Trainer")

        val query = dataB
            .whereEqualTo("UIDatleti", mAuth.currentUser?.uid)

        query.get().addOnSuccessListener { querySnapshot ->

            for (dc : DocumentChange in querySnapshot.documentChanges){
                note = dc.document.getString("UIDatleti").toString1()
                Log.d(TAG, "*************PROVAAAAAA ${note}")
            }
            if (mAuth.currentUser?.uid != note) {

                val utenti = hashMapOf(
                    "nome" to mAuth.currentUser?.displayName,
                    "UIDatleti" to (mAuth.uid.toString1()),
                    "IDfoto" to ID.toString()
                )

                db.collection("Personal Trainer").document(mAuth.currentUser?.uid.toString1())
                    .set(utenti)
                    .addOnSuccessListener {
                        Log.d(
                            TAG,
                            "DocumentSnapshot successfully written!"
                        )
                    }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }

                Log.d(TAG, "Dentro con registrazione utente")
                Toast.makeText(this, " Login effettuato ", Toast.LENGTH_SHORT).show()

                //apre TrainerActivity
                Intent(this, TrainerActivity::class.java).also {
                    startActivity(it)
                    it.putExtra("uidAtleti", mAuth.currentUser?.uid.toString1())
                }
            }else{
                Log.d(TAG, "Dentro senza registrazione utente")
                Toast.makeText(this, " Login effettuato ", Toast.LENGTH_SHORT).show()
                //apre TrainerActivity
                Intent(this, TrainerActivity::class.java).also {
                    startActivity(it)
                    intent.putExtra("uidAtleti", mAuth.currentUser?.uid.toString1())
                }
            }
        }
        Log.d(TAG, "updateUI: *****************${note}")
    }
}

