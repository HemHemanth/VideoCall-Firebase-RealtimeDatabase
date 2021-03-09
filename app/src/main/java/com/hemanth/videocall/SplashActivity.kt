package com.hemanth.videocall

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_splash.*
import java.util.*
import kotlin.collections.ArrayList

class SplashActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private var countries: ArrayList<String> = ArrayList()
    var mCountries = ArrayList<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        /*countries.add("Select Country")
        val isoCountries = Locale.getISOCountries()
        for (country in isoCountries) {
            val locale = Locale("en", country)
            countries.add(locale.displayCountry)
        }

        countries.sort()

        mCountries.add("Select Country")
        mCountries.addAll(countries)

        var adapter: ArrayAdapter<String> = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            mCountries
        )

        spinnerCountries.adapter = adapter

        spinnerCountries.onItemSelectedListener = this*/

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

//                Toast.makeText(this, token, Toast.LENGTH_LONG).show()

                var intent = Intent(this, RegistrationActivity::class.java)
                startActivity(intent)
                finish()
            }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (position != 0)
            Toast.makeText(this, mCountries[position], Toast.LENGTH_LONG).show()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
}