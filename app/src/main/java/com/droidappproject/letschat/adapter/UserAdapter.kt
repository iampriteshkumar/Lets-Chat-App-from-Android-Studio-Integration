package com.droidappproject.letschat.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.droidappproject.letschat.ChatActivity
import com.droidappproject.letschat.R
import com.droidappproject.letschat.databinding.ItemProfileBinding
import com.droidappproject.letschat.model.User

class UserAdapter(var context:Context, var userList: ArrayList<User>):
    RecyclerView.Adapter<UserAdapter.UserViewHolder>()
{

    inner class UserViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val binding: ItemProfileBinding = ItemProfileBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        var v = LayoutInflater.from(context).inflate(R.layout.item_profile, parent, false)
        return UserViewHolder(v)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {

        val user = userList[position]
        holder.binding.username.text = user.name
        Glide.with(context).load(user.profileImage)
            .placeholder(R.drawable.ic_avatar)
            .into(holder.binding.profile)
        holder.itemView.setOnClickListener{
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", user.name)
            intent.putExtra("image", user.profileImage)
            intent.putExtra("uid", user.uid)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = userList.size

}