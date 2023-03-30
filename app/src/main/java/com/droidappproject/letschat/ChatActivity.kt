package com.droidappproject.letschat

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.droidappproject.letschat.adapter.MessagesAdapter
import com.droidappproject.letschat.databinding.ActivityChatBinding
import com.droidappproject.letschat.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.util.Calendar
import java.util.Date

class ChatActivity : AppCompatActivity() {

    var binding: ActivityChatBinding? = null
    var adapter: MessagesAdapter? = null
    var messages: ArrayList<Message>? = null
    var senderRoom: String? = null
    var receiverRoom: String? = null
    var database: FirebaseDatabase? = null
    var storage: FirebaseStorage? = null
    var dialog: ProgressDialog? = null
    var senderUid: String? = null
    var receiverUid: String? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setSupportActionBar(binding!!.toolbar)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        dialog = ProgressDialog(this@ChatActivity)
        dialog!!.setMessage("Uploading image...")
        dialog!!.setCancelable(false)
        messages = ArrayList()
        val name = intent.getStringExtra("name")
        val profile = intent.getStringExtra("image")
        binding!!.name.text = name
        Glide.with(this@ChatActivity).load(profile)
            .placeholder(R.drawable.placeholder)
            .into(binding!!.profile01)
        binding!!.imageView2.setOnClickListener{ finish() }
        receiverUid = intent.getStringExtra("uid")
        senderUid = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence").child(receiverUid!!)
            .addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status = snapshot.getValue(String::class.java)
                        if (status == "offline") {
                            binding!!.status.visibility = View.GONE
                        }
                        else {
                            binding!!.status.setText(status)
                            binding!!.status.visibility = View.VISIBLE

                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) { }

            })
        senderRoom = senderUid + receiverUid
        receiverRoom =  receiverUid + senderUid
        adapter = MessagesAdapter(this@ChatActivity, messages, senderRoom!!, receiverRoom!!)

        binding!!.recyclerView.layoutManager = LinearLayoutManager(this@ChatActivity)
        binding!!.recyclerView.adapter = adapter
        database!!.reference.child("chats")
            .child(senderRoom!!)
            .child("message")
            .addValueEventListener(object :ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages!!.clear()
                    for (snapshot1 in snapshot.children){
                        val message: Message? = snapshot1.getValue(Message::class.java)
                        message!!.messageId = snapshot1.key
                        messages!!.add(message)

                    }
                    adapter!!.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) { }

            })
        binding!!.sendBtn.setOnClickListener{

            val messageTxt: String = binding!!.messageBox.text.toString()
            val date = Date()
            val message = Message(messageTxt, senderUid, date.time)


            binding!!.messageBox.setText("")
            val randomKey = database!!.reference.push().key
            val lastMsgObj = HashMap<String, Any>()
            lastMsgObj["lastMsg"] = message.message!!
            lastMsgObj["lastMsgTime"] = date.time

            database!!.reference.child("chats").child(senderRoom!!)
                .updateChildren(lastMsgObj)
            database!!.reference.child("chats").child(receiverRoom!!)
                .updateChildren(lastMsgObj)
            database!!.reference.child("chats").child(senderRoom!!)
                .child("messages")
                .child(randomKey!!)
                .setValue(message).addOnSuccessListener {
                    database!!.reference.child("chats")
                        .child(receiverRoom!!)
                        .child("message")
                        .child(randomKey)
                        .setValue(message)
                        .addOnSuccessListener {  }
                }

        }
        binding!!.attachment.setOnClickListener{
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 25)
        }

        val handler = Handler()
        binding!!.messageBox.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun afterTextChanged(p0: Editable?) {
                database!!.reference.child("Presence")
                    .child(senderUid!!)
                    .setValue("typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)
            }
            var userStoppedTyping = Runnable {
                database!!.reference.child("Presence")
                    .child(senderUid!!)
                    .setValue("online")
            }

        })
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 25) {
            if (data != null) {
                if (data.data != null) {
                    val selectedImage = data.data
                    val calender = Calendar.getInstance()
                    var refence = storage!!.reference.child("chats")
                        .child(calender.timeInMillis.toString()+"")
                    dialog!!.show()
                    refence.putFile(selectedImage!!)
                        .addOnCompleteListener{ task ->
                            dialog!!.dismiss()
                            if (task.isSuccessful) {
                                refence.downloadUrl.addOnSuccessListener { uri ->
                                    val filePath = uri.toString()
                                    val messageTxt:  String = binding!!.messageBox.text.toString()
                                    val date = Date()
                                    val message = Message(messageTxt, senderUid, date.time)
                                    message.message = "photo"
                                    message.imageUrl = filePath
                                    binding!!.messageBox.setText("")
                                    val randomkey = database!!.reference.push().key
                                    val lastMsgObj = HashMap<String, Any>()
                                    lastMsgObj["lastMsg"] = message.message!!
                                    lastMsgObj["lastMsgTime"] = date.time
                                    database!!.reference.child("chats")
                                        .updateChildren(lastMsgObj)
                                    database!!.reference.child("chats")
                                        .child(receiverRoom!!)
                                        .updateChildren(lastMsgObj)
                                    database!!.reference.child("chats")
                                        .child(senderRoom!!)
                                        .child("messages")
                                        .child(randomkey!!)
                                        .setValue(message).addOnSuccessListener {
                                            database!!.reference.child("chats")
                                                .child(receiverRoom!!)
                                                .child("messages")
                                                .child(randomkey)
                                                .setValue(message)
                                                .addOnSuccessListener {  }
                                        }
                                }
                            }
                        }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val currentId  = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence")
            .child(currentId!!)
            .setValue("online")
    }

    override fun onPause() {
        super.onPause()
        val currentId  = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence")
            .child(currentId!!)
            .setValue("offline")
    }


}