package com.hemanth.videocall.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hemanth.videocall.model.Contacts
import com.hemanth.videocall.R
import kotlinx.android.synthetic.main.contact_item.view.*

class ContactsAdapter(var context: Context, var contacts: ArrayList<Contacts>): RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder>() {

    class ContactsViewHolder(v: View): RecyclerView.ViewHolder(v) {
        var imgUserProfile: ImageView = v.imgUserProfile
        var txtUserName: TextView = v.txtUserName
        var btnVideoCall: Button = v.btnVideoCall
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        return ContactsViewHolder(
            LayoutInflater
                .from(
                    context
                )
                .inflate(
                    R.layout.contact_item,
                    parent,
                    false
                )
        )
    }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        holder.txtUserName.setText(contacts[position].name)

        Glide
            .with(context)
            .load(
                contacts[position].image
            )
            .into(holder.imgUserProfile)
    }

    override fun getItemCount(): Int = contacts.size
}