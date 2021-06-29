package com.example.whatsappclone.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.whatsappclone.R
import com.example.whatsappclone.models.UserModel
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : AppCompatActivity() {

    private val storage by lazy {
        FirebaseStorage.getInstance()
    }

    private val auth by lazy {
        FirebaseAuth.getInstance()
    }

    private val database by lazy {
        FirebaseFirestore.getInstance()
    }

    lateinit var downloadUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        userImgView.setOnClickListener {
            checkPermissionsForImage()
        }

        nextBtnProfile.setOnClickListener {
            nextBtnProfile.isEnabled = false
            var name: String = userName.text.toString()
            if(!:: downloadUrl.isInitialized) {
                showToast("Profile picture cannot be empty!")
            }
            else if(name.isEmpty()) {
                showToast("Username cannot be empty!")
                nextBtnProfile.isEnabled = true
            } else {
                val user = UserModel(
                    name, downloadUrl,
                    downloadUrl, auth.uid!!)

                database.collection("users").document(auth.uid!!).set(user).addOnSuccessListener {
                    startActivity(Intent(this, MainActivity::class.java).setFlags(
                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK))
                }.addOnFailureListener {
                    showToast("Upload failed. Try again!")
                    nextBtnProfile.isEnabled = true
                }
            }
        }
    }

    private fun showToast(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

    private fun checkPermissionsForImage() {

        if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)){

            val permissionRead = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            val permissionWrite = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

            requestPermissions(permissionRead, 1001)
            requestPermissions(permissionWrite, 1002)

        } else {
            pickImageFromGallery()
        }
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 1000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK && requestCode == 1000){
            data?.data?.let {
                userImgView.setImageURI(it)
                uploadImage(it)
            }
        }
    }

    private fun uploadImage(uri: Uri?) {
        nextBtnProfile.isEnabled = false
        val reference = storage.reference.child("uploads/" + auth.uid.toString())
        val uploadTask = reference.putFile(uri!!)

        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>>{
            if(!it.isSuccessful){
                it.exception?.let {
                    throw it
                }
            }
            return@Continuation reference.downloadUrl
        }).addOnCompleteListener {
            nextBtnProfile.isEnabled = true
            if(it.isSuccessful){
                downloadUrl = it.result.toString()
            } else {}
        }.addOnFailureListener {}
    }
}