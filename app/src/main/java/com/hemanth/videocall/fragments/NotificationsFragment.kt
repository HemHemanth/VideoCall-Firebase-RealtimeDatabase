package com.hemanth.videocall.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.hemanth.videocall.model.Contacts
import com.hemanth.videocall.NotificationsActivity
import com.hemanth.videocall.R
import kotlinx.android.synthetic.main.fragment_notifications.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class NotificationsFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var friendRequestsRef: DatabaseReference
    private lateinit var contactsRef: DatabaseReference
    private lateinit var usersRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUserId: String
    private var listUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

        var firebaseRecyclerAdapter = object: FirebaseRecyclerAdapter<Contacts, NotificationsActivity.Companion.NotificationsViewHolder>(options) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): NotificationsActivity.Companion.NotificationsViewHolder {
                var view = LayoutInflater
                    .from(
                        parent.context
                    )
                    .inflate(
                        R.layout.find_people_item,
                        parent,
                        false
                    )

                return NotificationsActivity.Companion.NotificationsViewHolder(view)
            }

            override fun onBindViewHolder(
                holder: NotificationsActivity.Companion.NotificationsViewHolder,
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

                                usersRef.child(listUserId!!).addValueEventListener(object:
                                    ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.hasChild("image")) {
                                            var imageStr = snapshot.child("image").value.toString()

                                            holder.userProfile?.let {
                                                context?.let { it1 ->
                                                    Glide
                                                        .with(
                                                            it1
                                                        )
                                                        .load(
                                                            imageStr
                                                        )
                                                        .into(
                                                            it
                                                        )
                                                }
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
            layoutManager = LinearLayoutManager(context)
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
                                                        Toast.makeText(context, "User Added Successfully", Toast.LENGTH_LONG).show()
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
                                Toast.makeText(context, "Friend Request Cancelled", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NotificationsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}