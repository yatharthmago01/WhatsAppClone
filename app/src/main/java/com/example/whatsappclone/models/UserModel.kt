package com.example.whatsappclone.models

import android.text.Editable
import com.google.firebase.firestore.FieldValue

data class UserModel(
    val name: String, val imageUrl: String,
    val thumbImageUrl: String, val uid: String,
    val deviceToken: String, val status: String, val onlineStatus: String) {

    constructor(): this("", "", "",
        "", "", "","")

    constructor(name: String, imageUrl: String, thumbImageUrl: String,
        uid: String): this(name, imageUrl, thumbImageUrl, uid,
            "", "Hey there I'm using whatsApp!", "")
}