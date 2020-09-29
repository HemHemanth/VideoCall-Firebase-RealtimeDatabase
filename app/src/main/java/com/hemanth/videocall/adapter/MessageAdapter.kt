package com.hemanth.videocall.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.hemanth.videocall.R
import com.hemanth.videocall.model.Chat

class MessageAdapter(var context: Context, var chat: List<Chat>, var imgUrl: String): RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {
    companion object {
        const val MSG_TYPE_LEFT = 0
        const val MSG_TYPE_RIGHT = 1
    }
    private var currentUser: String? = null
    class MessageViewHolder(v: View): RecyclerView.ViewHolder(v) {
        var message: TextView? = null
        var userProfile: ImageView? = null

        init {
            message = v.findViewById(R.id.message)
            userProfile = v.findViewById(R.id.userProfile)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        if (viewType == MSG_TYPE_RIGHT) {
            return MessageViewHolder(
                LayoutInflater
                    .from(
                        context
                    )
                    .inflate(
                        R.layout.chat_item_right,
                        parent,
                        false
                    )
            )
        } else {
            return MessageViewHolder(
                LayoutInflater
                    .from(
                        context
                    )
                    .inflate(
                        R.layout.chat_item_left,
                        parent,
                        false
                    )
            )
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.message?.setText(chat[position].message)

        holder.userProfile?.let {
            Glide
                .with(
                    context
                )
                .load(
                    imgUrl
                )
                .into(
                    it
                )
        }
    }

    override fun getItemCount(): Int = chat.size

    override fun getItemViewType(position: Int): Int {
        currentUser = FirebaseAuth.getInstance().currentUser?.uid

        if (chat[position].sender == currentUser) {
            return MSG_TYPE_RIGHT
        } else {
            return MSG_TYPE_LEFT
        }
    }
}