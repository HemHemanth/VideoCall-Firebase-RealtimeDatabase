package com.hemanth.videocall.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.hemanth.videocall.*
import com.hemanth.videocall.R
import com.hemanth.videocall.interfaces.IMyFriendsFragment
import com.hemanth.videocall.model.Contacts
import kotlinx.android.synthetic.main.activity_contacts.recyclerViewContacts
import kotlinx.android.synthetic.main.fragment_my_friends.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class MyFriendsFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var contactsRef: DatabaseReference
    private lateinit var userRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUserId: String
    private var friends: ArrayList<Contacts>? = null

    private var iMyProfileFragment: IMyFriendsFragment? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        iMyProfileFragment = context as IMyFriendsFragment
    }

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
        return inflater.inflate(R.layout.fragment_my_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contactsRef = FirebaseDatabase.getInstance().reference.child("Contacts")
        userRef = FirebaseDatabase.getInstance().reference.child("Users")

        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid.toString()

        findFriends.setOnClickListener {
            iMyProfileFragment?.onFindProfilesTapped()
            /*val intent = Intent(context, FindPeopleActivity::class.java).apply {

            }
            startActivity(intent)*/
        }
    }

    override fun onStart() {
        super.onStart()

        checkForReceivingCall()

        validateUser()

        var options = FirebaseRecyclerOptions.Builder<Contacts>()
            .setQuery(contactsRef.child(currentUserId),
                Contacts::class.java)
            .build()

        var firebaseRecyclerAdapter = object: FirebaseRecyclerAdapter<Contacts, ContactsActivity.Companion.ContactsViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsActivity.Companion.ContactsViewHolder {
                return ContactsActivity.Companion.ContactsViewHolder(
                    LayoutInflater
                        .from(
                            parent.context
                        )
                        .inflate(
                            R.layout.contact_item,
                            parent,
                            false
                        )
                )
            }

            override fun onBindViewHolder(
                holder: ContactsActivity.Companion.ContactsViewHolder,
                position: Int,
                contact: Contacts
            ) {
                var listUserId = getRef(position).key.toString()

                userRef.child(listUserId)
                    .addValueEventListener(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                var userName = snapshot.child("name").value.toString()
                                var profileImage = snapshot.child("image").value.toString()

                                holder.userName?.setText(userName)
                                holder.userProfile?.let {
                                    context?.let { it1 ->
                                        Glide
                                            .with(it1)
                                            .load(profileImage)
                                            .into(it)
                                    }
                                }

                                holder.btnVideoCall?.setOnClickListener {
                                    var intent = Intent(context, CallingActivity::class.java)
                                    intent.putExtra("userId", listUserId)
                                    startActivity(intent)
                                }

                                holder.btnChat?.setOnClickListener {
                                    var intent = Intent(context, MessagingActivity::class.java)
                                    intent.putExtra("userId", listUserId)
                                    startActivity(intent)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
            }

        }

        recyclerViewContacts.apply {
            adapter = firebaseRecyclerAdapter
            layoutManager = LinearLayoutManager(context)
            firebaseRecyclerAdapter.startListening()
        }
    }

    private fun checkForReceivingCall() {
        userRef.child(currentUserId)
            .child("Ringing")
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.hasChild("ringing")) {
                        var calledBy = snapshot.child("ringing").value.toString()

                        var intent = Intent(context, CallingActivity::class.java)
                        intent.putExtra("userId",calledBy)
                        startActivity(intent)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }


    private fun validateUser() {
        var dbReference = FirebaseDatabase.getInstance().reference

        dbReference
            .child(
                "Users"
            )
            .child(
                currentUserId
            )
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        var intent = Intent(context, SettingsActivity::class.java)
                        startActivity(intent)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }



    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MyFriendsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}