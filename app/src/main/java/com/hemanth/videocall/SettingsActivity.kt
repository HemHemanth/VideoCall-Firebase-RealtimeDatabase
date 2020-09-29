package com.hemanth.videocall

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {
    private var imageUri: Uri? = null
    private var downloadUri: String? = null
    private lateinit var currentUserId: String
    private lateinit var userProfileImageRef: StorageReference
    private lateinit var userRef: DatabaseReference
    private lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        progressDialog = ProgressDialog((this))

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        userProfileImageRef = FirebaseStorage.getInstance().reference.child("image")
        userRef = FirebaseDatabase.getInstance().reference.child("Users")
        retriveUserInfo()

        imgProfile.setOnClickListener {
            var intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        save.setOnClickListener {
            saveUserData()
        }
    }

    private fun saveUserData() {
        val userName = edtUserName.text.toString()
        val userStatus = edtUserStatus.text.toString()

        if (imageUri == null) {
            userRef.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (FirebaseAuth.getInstance().currentUser?.uid?.let { snapshot.child(it).hasChild("image") }!!) {
                        saveInfoWithoutProfileImage()
                    } else {
                        Toast.makeText(this@SettingsActivity, "Please select profile Image", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        } else if (userName == "") {
            Toast.makeText(this, "Please enter User Name", Toast.LENGTH_LONG).show()
        } else if (userStatus == "") {
            Toast.makeText(this, "Please enter User Status", Toast.LENGTH_LONG).show()
        } else {
            progressDialog.setTitle("Account Settings")
            progressDialog.setMessage("Please Wait...")
            progressDialog.show()
            val filePath: StorageReference? = FirebaseAuth.getInstance().currentUser?.uid?.let {
                userProfileImageRef.child(
                    it
                )
            }

            val uploadTask = imageUri?.let { filePath?.putFile(it) }
            uploadTask?.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw  it
                    }
                }
                downloadUri = filePath?.downloadUrl.toString()
                filePath?.downloadUrl!!
            }?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    downloadUri = task.result.toString()

                    var profileMap: HashMap<String, Any> = HashMap()
                    FirebaseAuth.getInstance().currentUser?.uid?.let {
                        profileMap.put(
                            "uid",
                            it
                        )
                    }
                    profileMap.put("name", userName)
                    profileMap.put("status", userStatus)
                    profileMap.put("image", downloadUri!!)

                    FirebaseAuth.getInstance().currentUser?.uid?.let {
                        userRef.child(it).updateChildren(profileMap).addOnCompleteListener {
                            if (it.isSuccessful) {
                                progressDialog.dismiss()
                                var intent =
                                    Intent(this@SettingsActivity, ContactsActivity::class.java)
                                startActivity(intent)
                                finish()

                                Toast.makeText(
                                    this@SettingsActivity,
                                    "Profile has been updated",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun saveInfoWithoutProfileImage() {
        progressDialog.setTitle("Account Settings")
        progressDialog.setMessage("Please Wait...")
        progressDialog.show()

        var userName = edtUserName.text.toString()
        var userStatus = edtUserStatus.text.toString()

        if (userName == "") {
            Toast.makeText(this, "Please enter User Name", Toast.LENGTH_LONG).show()
        } else if (userStatus == "") {
            Toast.makeText(this, "Please enter User Status", Toast.LENGTH_LONG).show()
        } else {
            var profileMap = HashMap<String, Any>()
            FirebaseAuth.getInstance().currentUser?.uid?.let {
                profileMap.put(
                    "uid",
                    it
                )
            }
            profileMap.put("name", userName)
            profileMap.put("status", userStatus)

            FirebaseAuth.getInstance().currentUser?.uid?.let {
                userRef.child(it)
                    .updateChildren(profileMap)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            progressDialog.dismiss()
                            var intent =
                                Intent(this@SettingsActivity, ContactsActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }
            }
        }
    }

    private fun retriveUserInfo() {
        FirebaseAuth.getInstance().currentUser?.uid?.let { userRef.child(it)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var imageDb = snapshot.child("image").getValue().toString()
                        var nameDb = snapshot.child("name").getValue().toString()
                        var statusDb = snapshot.child("status").getValue().toString()

                        edtUserName.setText(nameDb)
                        edtUserStatus.setText(statusDb)
                        Glide.with(this@SettingsActivity)
                            .load(imageDb)
                            .into(imgProfile)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            (requestCode == 1 && resultCode == RESULT_OK && data != null) -> {
                imageUri = data.data
                imgProfile.setImageURI(imageUri)

            }
        }
    }
}