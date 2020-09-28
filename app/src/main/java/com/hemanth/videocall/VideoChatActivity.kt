package com.hemanth.videocall

import android.Manifest
import android.content.Intent
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.opentok.android.*
import kotlinx.android.synthetic.main.activity_video_chat.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class VideoChatActivity : AppCompatActivity(), Session.SessionListener, PublisherKit.PublisherListener {
    var API_KEY: String = "46934724"
    var SESSION_ID: String = "1_MX40NjkzNDcyNH5-MTYwMTI5ODkwMjQ2MX55clhUeVluSThwYXRlVG9KTXlVL3FtNDl-fg"
    var TOKEN: String = "T1==cGFydG5lcl9pZD00NjkzNDcyNCZzaWc9ZDE0YmQ3N2UwMDlhMWYzZWViYjQ1YzQxZGIyYTBlOGU3MmEwYWE1ZDpzZXNzaW9uX2lkPTFfTVg0ME5qa3pORGN5Tkg1LU1UWXdNVEk1T0Rrd01qUTJNWDU1Y2xoVWVWbHVTVGh3WVhSbFZHOUtUWGxWTDNGdE5EbC1mZyZjcmVhdGVfdGltZT0xNjAxMjk4OTYwJm5vbmNlPTAuMDY5NzU1MDEyNDQ1OTM3MTgmcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTYwMzg5MDk1OCZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ=="
    lateinit var usersRef: DatabaseReference
    lateinit var userid: String
    private var session: Session? = null
    private var publisher: Publisher? = null
    private var subscriber: Subscriber? = null

    companion object {
        val LOG_TAG: String = VideoChatActivity::class.java.simpleName
        const val RC_VIDEO_APP_PERMISSION: Int = 124
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_chat)

        usersRef = FirebaseDatabase.getInstance().reference.child("Users")
        userid = FirebaseAuth.getInstance().currentUser?.uid.toString()

        closeChat.setOnClickListener {
            usersRef.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.child(userid).hasChild("Ringing")) {
                        usersRef.child(userid).child("Ringing").removeValue()

                        publisher?.destroy()
                        subscriber?.destroy()

                        startActivity(Intent(this@VideoChatActivity, RegistrationActivity::class.java))
                        finish()
                    }

                    if (snapshot.child(userid).hasChild("Calling")) {
                        usersRef.child(userid).child("Calling").removeValue()

                        publisher?.destroy()
                        subscriber?.destroy()

                        startActivity(Intent(this@VideoChatActivity, RegistrationActivity::class.java))
                        finish()
                    } else {
                        publisher?.destroy()
                        subscriber?.destroy()

                        startActivity(Intent(this@VideoChatActivity, RegistrationActivity::class.java))
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
        requestPermissions()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERMISSION)
    private fun requestPermissions() {
        var perms: Array<String> = arrayOf(Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)

        if (EasyPermissions.hasPermissions(this, *perms)) {
            session = com.opentok.android.Session.Builder(this, API_KEY, SESSION_ID).build()
            session?.setSessionListener(this)
            session?.connect(TOKEN)
        } else {
            EasyPermissions.requestPermissions(this, "Mic and Camera Permission Needed", RC_VIDEO_APP_PERMISSION)
        }
    }

    override fun onConnected(session: Session?) {
        publisher = Publisher.Builder(this).build()
        publisher?.setPublisherListener(this)
        publisherContainer.addView(publisher?.view)
        if (publisher?.view is GLSurfaceView) {
            (publisher?.view as GLSurfaceView).setZOrderOnTop(true)
        }

        session?.publish(publisher)
    }

    override fun onDisconnected(p0: Session?) {
    }

    override fun onStreamReceived(session: Session?, stream: Stream?) {
        if (subscriber == null) {
            subscriber = Subscriber.Builder(this, stream).build()
            session?.subscribe(subscriber)
            subscribeContainer.addView(subscriber?.view)
        }
    }

    override fun onStreamDropped(p0: Session?, p1: Stream?) {
        if (subscriber != null) {
            subscriber = null
            subscribeContainer.removeAllViews()
        }
    }

    override fun onError(p0: Session?, p1: OpentokError?) {
    }

    override fun onStreamCreated(p0: PublisherKit?, p1: Stream?) {
    }

    override fun onStreamDestroyed(p0: PublisherKit?, p1: Stream?) {
    }

    override fun onError(p0: PublisherKit?, p1: OpentokError?) {
    }
}