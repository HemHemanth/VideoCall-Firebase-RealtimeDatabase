package com.hemanth.videocall

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_notifications.*
import kotlinx.android.synthetic.main.find_people_item.view.*

class NotificationsActivity : AppCompatActivity() {
    private lateinit var friendRequestsRef: DatabaseReference
    private lateinit var contactsRef: DatabaseReference
    private lateinit var usersRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUserId: String
    private var listUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        friendRequestsRef = FirebaseDatabase.getInstance().reference.child("Friend Requests")
        contactsRef = FirebaseDatabase.getInstance().reference.child("Contacts")
        usersRef = FirebaseDatabase.getInstance().reference.child("Users")

        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid.toString()
    }

    override fun onStart() {
        super.onStart()

        var options = FirebaseRecyclerOptions
            .Builder<Contacts>()
            .setQuery(
                friendRequestsRef.child(currentUserId
                ),
                Contacts::class.java)
            .build()

        var firebaseRecyclerAdapter = object: FirebaseRecyclerAdapter<Contacts, NotificationsViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): NotificationsViewHolder {
                var view = LayoutInflater
                    .from(
                        parent.context
                    )
                    .inflate(
                        R.layout.find_people_item,
                        parent,
                        false
                    )

                return NotificationsViewHolder(view)
            }

            override fun onBindViewHolder(
                holder: NotificationsViewHolder,
                position: Int,
                contacts: Contacts
            ) {
                listUserId = getRef(position).key.toString()

                var requestTypeRef = getRef(position).child("request_type").ref

                requestTypeRef.addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            var type = snapshot.value.toString()

                            if (type == "received") {
                                holder.userItemView?.visibility = View.VISIBLE
                                holder.accept?.visibility = View.VISIBLE
                                holder.decline?.visibility = View.VISIBLE

                                usersRef.child(listUserId!!).addValueEventListener(object: ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.hasChild("image")) {
                                            var imageStr = snapshot.child("image").value.toString()

                                            holder.userProfile?.let {
                                                Glide
                                                    .with(
                                                        this@NotificationsActivity
                                                    )
                                                    .load(
                                                        imageStr
                                                    )
                                                    .into(
                                                        it
                                                    )
                                            }
                                            val nameStr = snapshot.child("name").value.toString()
                                            holder.userName?.setText(nameStr)
                                        }

                                        holder.accept?.setOnClickListener {
                                            acceptFriendRequest()
                                        }

                                        holder.decline?.setOnClickListener {
                                            cancelFriendRequest()
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {

                                    }
                                })
                            } else {
                                holder.userItemView?.visibility = View.GONE
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })
            }

        }

        recyclerNotifications.apply {
            adapter = firebaseRecyclerAdapter
            layoutManager = LinearLayoutManager(this@NotificationsActivity)
            firebaseRecyclerAdapter.startListening()
        }
    }

    private fun acceptFriendRequest() {
        contactsRef.child(
            currentUserId
        )
            .child(
                listUserId!!
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
                            listUserId!!
                        )
                        .child(
                            currentUserId
                        )
                        .child(
                            "Contacts"
                        )
                        .setValue(
                            "Saved"
                        )
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                friendRequestsRef
                                    .child(
                                        currentUserId
                                    )
                                    .child(
                                        listUserId!!
                                    )
                                    .removeValue()
                                    .addOnCompleteListener {
                                        if (it.isSuccessful) {
                                            friendRequestsRef
                                                .child(
                                                    listUserId!!
                                                )
                                                .child(
                                                    currentUserId
                                                )
                                                .removeValue()
                                                .addOnCompleteListener {
                                                    if (it.isSuccessful) {
                                                        Toast.makeText(this, "User Added Successfully", Toast.LENGTH_LONG).show()
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
        friendRequestsRef
            .child(
                currentUserId
            )
            .child(
                listUserId!!
            )
            .removeValue()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    friendRequestsRef
                        .child(
                            listUserId!!
                        )
                        .child(
                            currentUserId
                        )
                        .removeValue()
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(this, "Friend Request Cancelled", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }
    }


    companion object {
        class NotificationsViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            var userName: TextView? = null
            var userProfile: ImageView? = null
            var accept: Button? = null
            var decline: Button? = null
            var userItemView: CardView? = null

            init {
                userName = v.userName
                userProfile = v.userProfile
                accept = v.accept
                decline = v.decline
                userItemView = v.cardView
            }

        }
    }
}