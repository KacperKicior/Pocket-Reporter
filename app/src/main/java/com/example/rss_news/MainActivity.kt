package com.example.rss_news

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.rss_news.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    val auth by lazy { Firebase.auth }
    var oneTapClient: SignInClient? = null
    lateinit var signInRequest: BeginSignInRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.default_web_client_id))
                    // Only show accounts previously used to sign in.
                    .setFilterByAuthorizedAccounts(false)
                    .build())
            .build()

        binding.logButton.setOnClickListener(::onLogin)
        binding.signButton.setOnClickListener(::onSign)
        binding.signWithGoogleButton.setOnClickListener(::onGoogle)

    }

    private fun onGoogle(view: View?) {
        CoroutineScope(Dispatchers.Main).launch {
            signingGoogle()
        }
    }


    private suspend fun signingGoogle(){
        val result = oneTapClient?.beginSignIn(signInRequest)?.await()
        val intentSenderRequest = IntentSenderRequest.Builder(result!!.pendingIntent).build()
        activityResultLauncher.launch(intentSenderRequest)
    }

    private val activityResultLauncher: ActivityResultLauncher<IntentSenderRequest> =
        registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) {result ->
            if (result.resultCode == RESULT_OK){
                try {
                    val credential = oneTapClient!!.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    if (idToken != null){
                        val firebaseCredential = GoogleAuthProvider.getCredential(idToken,null)
                        auth.signInWithCredential(firebaseCredential).addOnCompleteListener {
                            if (it.isSuccessful){
                                startActivity(Intent(this,NewsListActivity::class.java))
                            }
                        }
                    }
                } catch (e: ApiException){
                    e.printStackTrace()
                }
            }
        }



    private fun onSign(view: View?) {
        if (binding.email.text.toString().isNullOrEmpty() || binding.password.text.toString().isNullOrEmpty()){
            Toast.makeText(this,"Invalid Credentials", Toast.LENGTH_LONG).show()
        }else {
            auth.createUserWithEmailAndPassword(
                binding.email.text.toString(),
                binding.password.text.toString()
            ).addOnSuccessListener {
                if (it.user != null) {
                    startActivity(Intent(this, NewsListActivity::class.java))
                }
            }.addOnFailureListener {
                Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onLogin(view: View?) {
        if (binding.email.text.toString().isNullOrEmpty() || binding.password.text.toString().isNullOrEmpty()){
            Toast.makeText(this,"Invalid Credentials", Toast.LENGTH_LONG).show()
        }else {
            auth.signInWithEmailAndPassword(
                binding.email.text.toString(),
                binding.password.text.toString()
            ).addOnSuccessListener {
                if (it.user != null) {
                    startActivity(Intent(this, NewsListActivity::class.java))
                }
            }.addOnFailureListener {
                Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
            }
        }
    }


}