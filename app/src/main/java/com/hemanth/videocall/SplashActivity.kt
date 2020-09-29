package com.hemanth.videocall

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.iid.FirebaseInstanceId

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        getCurrentRegistrationToken()
    }

    private fun getCurrentRegistrationToken() {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener {
                if (!it.isSuccessful) {
                    it.exception
                    return@addOnCompleteListener
                }

                var token = it.result?.token

                Toast.makeText(this, token, Toast.LENGTH_LONG).show()

                var intent = Intent(this, RegistrationActivity::class.java)
                startActivity(intent)
            }
    }
}