package com.hemanth.videocall

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_find_people.*
import kotlinx.android.synthetic.main.contact_item.view.*

class FindPeopleActivity: AppCompatActivity() {
    private var str: String? = null
    private lateinit var usersRef: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_people)

        usersRef = FirebaseDatabase.getInstance().reference.child("Users")
        search.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(charSec: CharSequence?, start: Int, end: Int, count: Int) {

            }

            override fun onTextChanged(charSec: CharSequence?, start: Int, end: Int, count: Int) {
                str = charSec.toString()
                onStart()
            }

            override fun afterTextChanged(e: Editable?) {

            }

        })
    }

    override fun onStart() {
        super.onStart()

        var options: FirebaseRecyclerOptions<Contacts>? = null

        if (str == "" || str == null) {
            options = FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(usersRef, Contacts::class.java)
                .build()
        } else {
            options = FirebaseRecyclerOptions
                .Builder<Contacts>()
                .setQuery(
                    usersRef
                        .orderByChild("name")
                        .startAt(str)
                        .endAt(str + "\uf8ff"),
                Contacts::class.java)
                .build()
        }

        var firebaseAdapter: FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder> =
            object: FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): FindFriendsViewHolder {
                    var view = LayoutInflater
                        .from(
                            parent.context
                        )
                        .inflate(
                            R.layout.contact_item,
                            parent,
                            false
                        )
                    var viewHolder = FindFriendsViewHolder(view)
                    return viewHolder
                }

                override fun onBindViewHolder(
                    holder: FindFriendsViewHolder,
                    position: Int,
                    contact: Contacts
                ) {
                    holder.userName?.setText(contact.getName())
                    holder.userProfile?.let {
                        Glide.with(this@FindPeopleActivity)
                            .load(contact.getImage())
                            .into(it)
                    }

                    holder.userItemView?.setOnClickListener {
                        var visitorUserId = getRef(position).key

                        var intent = Intent(this@FindPeopleActivity, UserProfileActivity::class.java)
                        intent.putExtra("name", contact.getName())
                        intent.putExtra("image", contact.getImage())
                        intent.putExtra("userId", visitorUserId)
                        startActivity(intent)
                    }

                }

            }
        recyclerViewFindContacts.apply {
            layoutManager = LinearLayoutManager(this@FindPeopleActivity)
            adapter = firebaseAdapter
        }

        firebaseAdapter.startListening()
    }

    companion object {
        class FindFriendsViewHolder(v: View) : RecyclerView.ViewHolder(v) {
            var userName: TextView? = null
            var userProfile: ImageView? = null
            var btnVideoCall: Button? = null
            var userItemView: CardView? = null

            init {
                userName = v.userName
                userProfile = v.userProfile
                btnVideoCall = v.btnVideoCall
                userItemView = v.userItemView

                btnVideoCall?.visibility = View.GONE
            }

        }
    }
}