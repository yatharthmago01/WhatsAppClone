package com.example.whatsappclone.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.UserManager
import com.example.whatsappclone.R
import com.example.whatsappclone.models.InboxModel
import com.example.whatsappclone.models.Message
import com.example.whatsappclone.models.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.android.synthetic.main.activity_chat.*
import java.util.*

const val UID = "uid"
const val NAME = "name"
const val IMAGE = "photo"

class ChatActivity : AppCompatActivity() {

    private val friendId: String by lazy {
        intent.getStringExtra(UID)!!
    }

    private val name: String by lazy {
        intent.getStringExtra(NAME)!!
    }

    private val image: String by lazy {
        intent.getStringExtra(IMAGE)!!
    }

    private val mCurrentId: String by lazy {
        FirebaseAuth.getInstance().uid!!
    }

    private val db: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    lateinit var currentUser: UserModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
        setContentView(R.layout.activity_chat)

        FirebaseFirestore.getInstance().collection("users").document(mCurrentId).get()
            .addOnSuccessListener {
                currentUser = it.toObject(UserModel::class.java)!!
        }

        nameTv.text = name
        Picasso.get().load(image).into(userImgView)

        sendBtn.setOnClickListener {
            msgEditTv.text?.let {
                if(it.isNotEmpty()){
                    sendMessage(it.toString())
                    it.clear()
                }
            }
        }
    }

    private fun sendMessage(msg: String) {
        val id = getMessages(friendId).push().key
        checkNotNull(id)
        val msgMap = Message(msg, mCurrentId, id)
        getMessages(friendId).child(id).setValue(msgMap).addOnSuccessListener {}
        
        updateLastMessage(msgMap)
    }

    private fun updateLastMessage(message: Message) {

        val inboxMap = InboxModel(message.msg, friendId, name, image, 0, message.sentAt)
        getInbox(mCurrentId, friendId).setValue(inboxMap).addOnSuccessListener {
            getInbox(friendId, mCurrentId).addListenerForSingleValueEvent(object :ValueEventListener{

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue(InboxModel::class.java)

                    inboxMap.apply {
                        from = message.senderId
                        name = currentUser.name
                        image = currentUser.thumbImageUrl
                        count = 1
                    }
                    value?.let {
                        if(it.from == message.senderId)
                            inboxMap.count = value.count + 1
                    }
                    getInbox(friendId, mCurrentId).setValue(inboxMap)
                }
            })
        }
    }

    private fun markAsRead(){
        getInbox(friendId, mCurrentId).child("count").setValue(0)
    }

    private fun getInbox(toUser: String, fromUser: String) =
        db.reference.child("chats/$toUser/$fromUser")

    private fun getMessages(friendId: String) =
        db.reference.child("messages/${getId(friendId)}")

    private fun getId(friendId: String): String{
        return if(friendId > mCurrentId)
            mCurrentId + friendId
        else
            friendId + mCurrentId
    }
}