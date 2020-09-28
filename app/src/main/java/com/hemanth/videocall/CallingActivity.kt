package com.hemanth.videocall

import android.content.Intent
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_calling.*

class CallingActivity : AppCompatActivity() {
    private lateinit var userRef: DatabaseReference
    private lateinit var receiverUserId: String
    private var receiverUserImage: String? = null
    private var receiverUserName: String? = null
    private lateinit var currentUserId: String
    private var currentUserImage: String? = null
    private var currentUserName: String? = null
    private var checker: String = ""
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calling)

        receiverUserId = intent.getStringExtra("userId").toString()

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        userRef = FirebaseDatabase.getInstance().reference.child("Users")

        mediaPlayer = MediaPlayer.create(this, R.raw.ringing)

        getAndSetUserProfile()

        makeCall.setOnClickListener {
            mediaPlayer.stop()
            val callingPickupMap = HashMap<String, Any> ()
            callingPickupMap.put("picked", "picked")

            userRef.child(currentUserId)
                .child("Ringing")
                .updateChildren(callingPickupMap)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        var intent = Intent(this, VideoChatActivity::class.java)
                        startActivity(intent)
                    }
                }
        }

        rejectCall.setOnClickListener {
            checker = "checked"
            mediaPlayer.stop()
            rejectCall()
        }
    }

    override fun onStart() {
        super.onStart()

        mediaPlayer.start()

        userRef.child(receiverUserId)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (checker != "checked" && !snapshot.hasChild("Calling") && !snapshot.hasChild("Ringing")) {
                        mediaPlayer.start()
                        var callingMap: HashMap<String, Any> = HashMap()
                        callingMap.put("calling", receiverUserId)

                        userRef.child(currentUserId)
                            .child("Calling")
                            .updateChildren(callingMap)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    var ringingMap: HashMap<String, Any> = HashMap()
                                    ringingMap.put("ringing", currentUserId)

                                    userRef.child(receiverUserId)
                                        .child("Ringing")
                                        .updateChildren(ringingMap)
                                }
                            }
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        userRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(currentUserId).hasChild("Ringing") && !snapshot.child(currentUserId).hasChild("Calling")) {
                    makeCall.visibility = View.VISIBLE
                }

                if (snapshot.child(receiverUserId)
                        .child("Ringing").hasChild("picked")) {
                    mediaPlayer.stop()
                    var intent = Intent(this@CallingActivity, VideoChatActivity::class.java)
                    startActivity(intent)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun getAndSetUserProfile() {
        userRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.child(receiverUserId).exists()) {
                    receiverUserImage = snapshot.child(receiverUserId).child("image").value.toString()
                    receiverUserName = snapshot.child(receiverUserId).child("name").value.toString()

                    userName.setText(receiverUserName)
                    receiverUserImage?.let {
                        Glide
                            .with(
                                this@CallingActivity
                            )
                            .load(
                                it
                            )
                            .into(userProfile)
                    }

                }

                if (snapshot.child(currentUserId).exists()) {
                    currentUserImage = snapshot.child(currentUserId).child("image").value.toString()
                    currentUserName = snapshot.child(currentUserId).child("name").value.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun rejectCall() {
        userRef.child(currentUserId).child("Calling")
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.hasChild("calling")) {
                        var callingId = snapshot.child("calling").value.toString()

                        userRef.child(callingId)
                            .child("Ringing")
                            .removeValue()
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    userRef.child(currentUserId)
                                        .child("Calling")
                                        .removeValue()
                                        .addOnCompleteListener {
                                            var intent = Intent(this@CallingActivity, RegistrationActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                }
                            }
                    } else {
                        var intent = Intent(this@CallingActivity, RegistrationActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        //Receiver
        userRef.child(currentUserId).child("Ringing")
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.hasChild("ringing")) {
                        var ringingId = snapshot.child("ringing").value.toString()

                        userRef.child(ringingId)
                            .child("Calling")
                            .removeValue()
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    userRef.child(currentUserId)
                                        .child("Ringing")
                                        .removeValue()
                                        .addOnCompleteListener {
                                            var intent = Intent(this@CallingActivity, RegistrationActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                }
                            }
                    } else {
                        var intent = Intent(this@CallingActivity, RegistrationActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })


    }
}