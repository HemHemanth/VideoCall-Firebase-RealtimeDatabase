package com.hemanth.videocall

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.hemanth.videocall.adapter.ContactsAdapter
import kotlinx.android.synthetic.main.activity_contacts.*
import kotlinx.android.synthetic.main.contact_item.view.*

class ContactsActivity : AppCompatActivity() {
    private lateinit var navView: BottomNavigationView
    private lateinit var contactsRef: DatabaseReference
    private lateinit var userRef: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUserId: String
    private var friends: ArrayList<Contacts>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)
         navView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener)

        contactsRef = FirebaseDatabase.getInstance().reference.child("Contacts")
        userRef = FirebaseDatabase.getInstance().reference.child("Users")

        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid.toString()

        findPeople.setOnClickListener {
            var intent = Intent(this, FindPeopleActivity::class.java)
            startActivity(intent)
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

        var firebaseRecyclerAdapter = object: FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
                return ContactsViewHolder(
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
                holder: ContactsViewHolder,
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
                                    Glide
                                        .with(this@ContactsActivity)
                                        .load(profileImage)
                                        .into(it)
                                }

                                holder.btnVideoCall?.setOnClickListener {
                                    var intent = Intent(this@ContactsActivity, CallingActivity::class.java)
                                    intent.putExtra("userId", listUserId)
                                    startActivity(intent)
                                }

                                holder.btnChat?.setOnClickListener {
                                    var intent = Intent(this@ContactsActivity, MessagingActivity::class.java)
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
            layoutManager = LinearLayoutManager(this@ContactsActivity)
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

                        var intent = Intent(this@ContactsActivity, CallingActivity::class.java)
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
                        var intent = Intent(this@ContactsActivity, SettingsActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    var navigationItemSelectedListener = object: BottomNavigationView.OnNavigationItemSelectedListener {
        override fun onNavigationItemSelected(item: MenuItem): Boolean {
            when(item.itemId) {
                R.id.navigation_home -> {
                    var homeIntent = Intent(this@ContactsActivity, ContactsActivity::class.java)
                    startActivity(homeIntent)
                }

                R.id.navigation_settings -> {
                    var settingsIntent = Intent(this@ContactsActivity, SettingsActivity::class.java)
                    startActivity(settingsIntent)
                }

                R.id.navigation_notifications -> {
                    var notificationsIntent = Intent(this@ContactsActivity, NotificationsActivity::class.java)
                    startActivity(notificationsIntent)
                }

                R.id.navigation_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    var logoutIntent = Intent(this@ContactsActivity, RegistrationActivity::class.java)
                    startActivity(logoutIntent)
                    finish()
                }
            }
            return true
        }

    }

    companion object {
        class ContactsViewHolder(v: View): RecyclerView.ViewHolder(v) {
            var userName: TextView? = null
            var userProfile: ImageView? = null
            var btnVideoCall: Button? = null
            var btnChat: Button? = null

            init {
                userName = v.txtUserName
                userProfile = v.imgUserProfile
                btnVideoCall = v.btnVideoCall
                btnChat = v.btnChat
            }

        }
    }
}