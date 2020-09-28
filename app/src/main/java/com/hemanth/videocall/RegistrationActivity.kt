package com.hemanth.videocall

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_registration.*
import java.util.concurrent.TimeUnit

class RegistrationActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    var mRecendToken: PhoneAuthProvider.ForceResendingToken? = null
    var mVerifycationId: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        auth = FirebaseAuth.getInstance()

        var callbacks = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                e.printStackTrace()
                Toast.makeText(this@RegistrationActivity, "Invalid Phone Nummber", Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(s: String, forceResendingToken: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(s, forceResendingToken)

                mVerifycationId = s
                mRecendToken = forceResendingToken

                submit.text = "Submit"

                txtEnterMobileNumber.visibility = View.GONE
                txtMobileNumber.visibility = View.GONE
                verificationCode.visibility = View.VISIBLE

                Toast.makeText(this@RegistrationActivity, "Code has been sent", Toast.LENGTH_LONG).show()

            }

        }

        submit.setOnClickListener {
            if (submit.text == "Submit") {
                var verificationCode = verificationCode.text.toString()

                if (verificationCode == "") {
                    Toast.makeText(this, "Please Enter Verification code", Toast.LENGTH_LONG).show()
                } else {
                    val credential =
                        mVerifycationId?.let { PhoneAuthProvider.getCredential(it, verificationCode) }
                    if (credential != null) {
                        signInWithPhoneAuthCredential(credential)
                    }
                }
            } else {
                var phoneNumber = txtMobileNumber.text.toString()
                PhoneAuthProvider.getInstance().verifyPhoneNumber(
                    phoneNumber, // Phone number to verify
                    60, // Timeout duration
                    TimeUnit.SECONDS, // Unit of timeout
                    this, // Activity (for callback binding)
                    callbacks
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        var firebaseUser = FirebaseAuth.getInstance().currentUser

        if (firebaseUser != null) {
            var intent = Intent(this, ContactsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Toast.makeText(this, "Logged in Successfully", Toast.LENGTH_LONG).show()
                    sendUserToMainActivity()

                    val user = task.result?.user
                    // ...
                } else {
                    // Sign in failed, display a message and update the UI
                    var e = task.exception.toString()
                    Toast.makeText(this, e, Toast.LENGTH_LONG).show()

                }
            }
    }

    private fun sendUserToMainActivity() {
        var intent = Intent(this, ContactsActivity::class.java)
        startActivity(intent)
    }
}