package com.hemanth.videocall

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.bumptech.glide.Glide
import com.google.android.gms.dynamic.IFragmentWrapper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_user_profile.*

class UserProfileActivity : AppCompatActivity() {
    private lateinit var receiverUserId: String
    private lateinit var auth: FirebaseAuth
    private lateinit var senderUserId: String
    private var currentState: String = "new"
    private lateinit var friendRequestRef: DatabaseReference
    private lateinit var contactsRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        auth = FirebaseAuth.getInstance()
        senderUserId = auth.currentUser?.uid.toString()
        friendRequestRef = FirebaseDatabase.getInstance().reference.child("Friend Requests")
        contactsRef = FirebaseDatabase.getInstance().reference.child("Contacts")

        var userName = intent.getStringExtra("name")
        var image = intent.getStringExtra("image")
        receiverUserId = intent.getStringExtra("userId").toString()

        if (senderUserId == receiverUserId) {
            addFriend.visibility = View.GONE
        }

       /* contactsRef.
        child(
            senderUserId
        )
            .addValueEventListener(
                object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.hasChild(receiverUserId)) {
                            var requestType = snapshot.child("Contacts").value.toString()
                            if (requestType == "Saved") {
                                currentState = "friends"
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                }
            )*/

        friendRequestRef.child(senderUserId)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild(receiverUserId)) {
                        var requestType = snapshot.child(receiverUserId).child("request_type").value.toString()
                        if (requestType == "sent") {
                            currentState = "request_sent"
                            addFriend.setText("Cancel Friend Request")
                        } else if (requestType == "received") {
                            currentState = "request_received"
                            addFriend.setText("Accept Friend Request")
                            declineFriend.visibility = View.VISIBLE
                        }
                    } else {
                        contactsRef
                            .child(senderUserId)
                            .addListenerForSingleValueEvent(object: ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.hasChild(receiverUserId)) {
                                        currentState = "friends"
                                        addFriend.setText("Delete Contact")
                                    } else {
                                        currentState = "new"
                                        addFriend.setText("Add Friend")
                                        declineFriend.visibility = View.GONE
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }

                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        edtUserName.setText(userName)
        Glide
            .with(this)
            .load(image)
            .into(userProfile)

        addFriend.setOnClickListener {
            if (currentState =="new") {
                sendFriendRequest()
            }
            if (currentState == "request_received") {
                acceptFriendRequest()
            }
            if (currentState == "request_sent") {
                cancelFriendRequest()
            }
            if (currentState == "friends") {
                deleteContact()
            }
        }

        declineFriend.setOnClickListener {
            cancelFriendRequest()
        }
    }

    private fun sendFriendRequest() {
        friendRequestRef.
        child(
            senderUserId
        )
            .child(
                receiverUserId
            )
            .child(
                "request_type"
            )
            .setValue(
                "sent"
            )
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    friendRequestRef
                        .child(
                            receiverUserId
                        )
                        .child(
                            senderUserId
                        )
                        .child(
                            "request_type"
                        )
                        .setValue(
                            "received"
                        )
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                currentState = "request_sent"
                                addFriend.setText("Cancel Friend Request")
                            }
                        }
                }
            }
    }

    private fun acceptFriendRequest() {
        contactsRef.child(
            senderUserId
        )
            .child(
                receiverUserId
            )
            .child(
                "Contacts"
            )
            .setValue(
                "Saved"
            )
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    contactsRef
                        .child(
                            receiverUserId
                        )
                        .child(
                            senderUserId
                        )
                        .child(
                            "Contacts"
                        )
                        .setValue(
                            "Saved"
                        )
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                friendRequestRef
                                    .child(
                                        senderUserId
                                    )
                                    .child(
                                        receiverUserId
                                    )
                                    .removeValue()
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            friendRequestRef
                                                .child(
                                                    receiverUserId
                                                )
                                                .child(
                                                    senderUserId
                                                )
                                                .removeValue()
                                                .addOnCompleteListener {
                                                    if (it.isSuccessful) {
                                                        currentState = "friends"
                                                        addFriend.setText("Delete Contact")
                                                        declineFriend.visibility = View.GONE
                                                    }
                                                }
                                        }
                                    }

                            }
                        }
                }
            }
    }

    private fun cancelFriendRequest() {
        friendRequestRef
            .child(
                senderUserId
            )
            .child(
                receiverUserId
            )
            .removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    friendRequestRef
                        .child(
                            receiverUserId
                        )
                        .child(
                            senderUserId
                        )
                        .removeValue()
                        .addOnCompleteListener {
                            addFriend.setText("Add Friend")
                            currentState = "new"
                            declineFriend.visibility = View.GONE
                        }
                }
            }
    }

    private fun deleteContact() {
        contactsRef
            .child(
                senderUserId
            )
            .child(
                receiverUserId
            )
            .removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    contactsRef.child(
                        receiverUserId
                    )
                        .child(
                            senderUserId
                        )
                        .removeValue()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                currentState = "new"
                                addFriend.setText("Add Friend")
                            }
                        }
                }
            }
    }
}