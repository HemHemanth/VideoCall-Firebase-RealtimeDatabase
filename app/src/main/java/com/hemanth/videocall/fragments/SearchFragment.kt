package com.hemanth.videocall.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.hemanth.videocall.model.Contacts
import com.hemanth.videocall.FindPeopleActivity
import com.hemanth.videocall.R
import com.hemanth.videocall.UserProfileActivity
import kotlinx.android.synthetic.main.fragment_search.*

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class SearchFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var str: String? = null
    private lateinit var usersRef: DatabaseReference

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
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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

        var firebaseAdapter: FirebaseRecyclerAdapter<Contacts, FindPeopleActivity.Companion.FindFriendsViewHolder> =
            object: FirebaseRecyclerAdapter<Contacts, FindPeopleActivity.Companion.FindFriendsViewHolder>(options) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): FindPeopleActivity.Companion.FindFriendsViewHolder {
                    var view = LayoutInflater
                        .from(
                            parent.context
                        )
                        .inflate(
                            R.layout.contact_item,
                            parent,
                            false
                        )
                    var viewHolder = FindPeopleActivity.Companion.FindFriendsViewHolder(view)
                    return viewHolder
                }

                override fun onBindViewHolder(
                    holder: FindPeopleActivity.Companion.FindFriendsViewHolder,
                    position: Int,
                    contact: Contacts
                ) {
                    holder.userName?.setText(contact.name)
                    holder.userProfile?.let {
                        context?.let { it1 ->
                            Glide.with(it1)
                                .load(contact.image)
                                .into(it)
                        }
                    }

                    holder.userItemView?.setOnClickListener {
                        var visitorUserId = getRef(position).key

                        var intent = Intent(context, UserProfileActivity::class.java)
                        intent.putExtra("name", contact.name)
                        intent.putExtra("image", contact.image)
                        intent.putExtra("userId", visitorUserId)
                        startActivity(intent)
                    }

                }

            }
        recyclerViewFindContacts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = firebaseAdapter
        }

        firebaseAdapter.startListening()
    }


    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}