package com.hemanth.videocall

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.hemanth.videocall.model.Contacts
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.activity_edit_profile.edtUserName
import kotlinx.android.synthetic.main.activity_edit_profile.save

class EditProfileActivity : AppCompatActivity() {
    private var imageUri: Uri? = null
    private var downloadUri: String? = null
    private lateinit var currentUserId: String
    private lateinit var userRef: DatabaseReference
    private lateinit var userProfileImageRef: StorageReference
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        userRef = FirebaseDatabase.getInstance().reference.child("Users")
        userProfileImageRef = FirebaseStorage.getInstance().reference.child("image")

        retriveUserInfo()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading Profile")
        progressDialog.setMessage("Please wait....")

        imgUserProfile.setOnClickListener {
            var intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(intent, PROFILE_PIC_REQUEST)
        }

        save.setOnClickListener {
            saveUserData()
        }
    }

    private fun saveUserData() {
        var userName = edtUserName.text.toString()
        var userBio = edtUserBio.text.toString()
        when {
            imageUri == null -> {
                userRef.addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.child(currentUserId).hasChild("image")) {
                            saveImageWithoutImage()
                        } else {
                            Snackbar.make(edit_profile_layout, "Please select Profile Picture", Snackbar.LENGTH_LONG)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
            }
            userName == "" -> {
                Snackbar.make(edit_profile_layout, "Please Enter User Name", Snackbar.LENGTH_LONG)
            }
            userBio == "" -> {
                Snackbar.make(edit_profile_layout, "Please Enter Bio", Snackbar.LENGTH_LONG)
            }
            else -> {
                progressDialog.show()
                var filePath: StorageReference = userProfileImageRef.child(currentUserId)

                val uploadTask = imageUri?.let { filePath.putFile(it) }

                uploadTask?.continueWith {
                    if (!it.isSuccessful) {
                        it.exception?.let {
                            throw it
                        }
                    }
                    downloadUri = filePath.downloadUrl.toString()
                    filePath.downloadUrl
                }?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        downloadUri = it.result.toString()

                        var profileMap: HashMap<String, Any> = HashMap()
                        FirebaseAuth.getInstance().currentUser?.uid?.let { it1 ->
                            profileMap.put("uid",
                                it1
                            )
                        }
                        profileMap.put("name", userName)
                        profileMap.put("bio", userBio)
                        profileMap.put("image", downloadUri!!)

                        FirebaseAuth.getInstance().currentUser?.uid?.let { it1 -> userRef.child(it1)
                            .updateChildren(profileMap)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    progressDialog.dismiss()
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    private fun saveImageWithoutImage() {
        var userName = edtUserName.text.toString()
        var userBio = edtUserBio.text.toString()
        when {
            userName == "" -> {
                Snackbar.make(edit_profile_layout, "Please Enter User Name", Snackbar.LENGTH_LONG)
            }
            userBio == "" -> {
                Snackbar.make(edit_profile_layout, "Please Enter Bio", Snackbar.LENGTH_LONG)
            }
            else -> {
                var profileMap: HashMap<String, Any> = HashMap()
                FirebaseAuth.getInstance().currentUser?.uid?.let { it1 ->
                    profileMap.put("uid",
                        it1
                    )
                }
                profileMap.put("name", userName)
                profileMap.put("bio", userBio)

                FirebaseAuth.getInstance().currentUser?.uid?.let {
                    userRef.child(it)
                        .updateChildren(profileMap)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when {
            requestCode == PROFILE_PIC_REQUEST && resultCode == RESULT_OK && data != null -> {
                imageUri = data.data
                imgUserProfile.setImageURI(imageUri)
            }
        }

    }

    private fun retriveUserInfo() {
        FirebaseAuth.getInstance().currentUser?.uid?.let { userRef.child(it)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        var contacts = snapshot.getValue(Contacts::class.java)
                        var imageDb = contacts?.image
                        var nameDb = contacts?.name
                        var bioDb = contacts?.status

                        edtUserName.setText(nameDb)
                        edtUserBio.setText(bioDb)
                        Glide.with(this@EditProfileActivity)
                            .load(imageDb)
                            .placeholder(R.drawable.user_profile)
                            .into(imgUserProfile)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        }
    }

    companion object {
        const val PROFILE_PIC_REQUEST = 100
    }
}