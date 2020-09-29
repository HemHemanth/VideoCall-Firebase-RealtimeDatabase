package com.hemanth.videocall

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.hemanth.videocall.adapter.MessageAdapter
import com.hemanth.videocall.model.Chat
import kotlinx.android.synthetic.main.activity_messaging.*

class MessagingActivity : AppCompatActivity() {
    private lateinit var userRef: DatabaseReference
    private lateinit var currentUserId: String
    private var chats: ArrayList<Chat> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messaging)

        var userId = intent.getStringExtra("userId").toString()

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()

        imgSend.setOnClickListener {
            var message = edtMessage.text.toString()
            if (message != "") {
                sendMessage(currentUserId, userId, message)
            }
            edtMessage.setText("")
        }

        userRef = FirebaseDatabase.getInstance().reference.child("Users")

        userRef.child(userId)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var contact: Contacts? = snapshot.getValue(Contacts::class.java)
                    var userName = contact?.getName()
                    var userImage = contact?.getImage()
                    txtUserName.setText(userName)
                    Glide
                        .with(
                            this@MessagingActivity
                        )
                        .load(
                            userImage
                        )
                        .into(imgUserProfile)

                    readMessage(currentUserId, userId, userImage!!)
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })

        imgBack.setOnClickListener {
            finish()
        }
    }

    private fun sendMessage(sender: String, receiver: String, message: String) {
        var reference: DatabaseReference = FirebaseDatabase.getInstance().reference
        var hashMap: HashMap<String, Any> = HashMap()
        hashMap.put("sender", sender)
        hashMap.put("receiver", receiver)
        hashMap.put("message", message)

        reference
            .child(
                "Chat"
            )
            .push()
            .setValue(
                hashMap
            )
    }

    private fun readMessage(sender: String, receiver: String, imgUrl: String) {
        var reference = FirebaseDatabase.getInstance().getReference("Chat")

        reference.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snapchat in snapshot.children) {
                    var chat: Chat? = snapchat.getValue(Chat::class.java)
                    if (chat != null && (chat.receiver == sender && chat.sender == receiver ||
                            chat.receiver == receiver && chat.sender == sender)) {
                        chats.add(chat)
                    }
                }

                recyclerViewMessages.apply {
                    layoutManager = LinearLayoutManager(this@MessagingActivity)
                    adapter = MessageAdapter(this@MessagingActivity, chats, imgUrl)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }
}