package com.droidappproject.letschat.adapter

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.droidappproject.letschat.R
import com.droidappproject.letschat.databinding.DeleteLayoutBinding
import com.droidappproject.letschat.databinding.ReceiveMsgBinding
import com.droidappproject.letschat.databinding.SendMsgBinding
import com.droidappproject.letschat.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MessagesAdapter(
    var context: Context,
    messages: ArrayList<Message>?,
    senderRoom: String,
    receiverRoom: String
): RecyclerView.Adapter<RecyclerView.ViewHolder?>()
{
    lateinit var messages: ArrayList<Message>
    val ITEM_SENT = 1
    val ITEM_RECEIVE = 2
    var senderRoom: String
    var receiverRoom: String

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return if (viewType == ITEM_SENT) {
            val view =  LayoutInflater.from(context).inflate(R.layout.send_msg,
                parent, false)
            SentViewHolder(view)
        }
        else {
            val view = LayoutInflater.from(context).inflate(R.layout.receive_msg,
                parent, false)
            ReceiverViewHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val  messages = messages[position]
        return if (FirebaseAuth.getInstance().uid == messages.senderId) {
            ITEM_SENT
        }else {
            ITEM_RECEIVE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val message = messages[position]
        if (holder.javaClass == SentViewHolder::class.java) {
            val viewHolder = holder as SentViewHolder
            if (message.message.equals("photo")) {
                viewHolder.binding.image.setVisibility(View.VISIBLE)
                viewHolder.binding.message.setVisibility(View.GONE)
//                viewHolder.binding.mLinear.setVisibility(View.GONE)
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(viewHolder.binding.image)
            }
            viewHolder.binding.message.text = message.message
            viewHolder.itemView.setOnLongClickListener {

                val view: View = LayoutInflater.from(context).inflate(R.layout.delete_layout, null)
                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)
                val dialog:AlertDialog = AlertDialog.Builder(context)
                    .setTitle("Delete message?")
                    .setView(binding.getRoot())
                    .create()
                binding.everyone.setOnClickListener{
                    message.message = "\uD83D\uDEAB This message was deleted."
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1).setValue(message)

                    }
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("chats")
                            .child(receiverRoom)
                            .child("message")
                            .child(it1).setValue(message)
                    }
                    dialog.dismiss()
                }

                binding.delete.setOnClickListener{
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1).setValue(null)
                    }
                    dialog.dismiss()

                }
                binding.cancel.setOnClickListener{ dialog.dismiss() }
                dialog.show()
                false
            }
        } else {
            val viewHolder = holder as ReceiverViewHolder
            if (message.message.equals("photo")) {

                viewHolder.binding.image.setVisibility(View.VISIBLE)
                viewHolder.binding.message.setVisibility(View.GONE)
//                viewHolder.binding.mLinear.setVisibility(View.GONE)
                Glide.with(context)
                    .load(message.imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .into(viewHolder.binding.image)

            }
            viewHolder.binding.message.setText(message.message)
            viewHolder.itemView.setOnLongClickListener {

                val view: View = LayoutInflater.from(context)
                    .inflate(R.layout.delete_layout, null)
                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)

                val dialog: AlertDialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.getRoot())
                    .create()
                binding.everyone.setOnClickListener{
                    message.message = "\uD83D\uDEAB This message was deleted."
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1).setValue(message)

                    }
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(receiverRoom)
                            .child("message")
                            .child(it1).setValue(message)
                    }
                    dialog.dismiss()
                }

                binding.delete.setOnClickListener{
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance().reference.child("chats")
                            .child(senderRoom)
                            .child("message")
                            .child(it1).setValue(null)
                    }
                    dialog.dismiss()

                }
                binding.cancel.setOnClickListener{ dialog.dismiss() }
                dialog.show()
                false

            }
        }

    }

    override fun getItemCount(): Int {
        return messages.size
    }

    inner class SentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var binding: SendMsgBinding= SendMsgBinding.bind(itemView)
    }

    inner class ReceiverViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var binding: ReceiveMsgBinding = ReceiveMsgBinding.bind(itemView)
    }
    init {
        if (messages != null) {
            this.messages = messages
        }
        this.senderRoom = senderRoom
        this.receiverRoom = receiverRoom
    }

}