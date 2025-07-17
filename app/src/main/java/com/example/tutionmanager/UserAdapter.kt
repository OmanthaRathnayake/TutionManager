package com.example.tutionmanager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter (
    private val users: List<User>,
    private val onEdit: (User) -> Unit,
    private val onDelete: (User) -> Unit
): RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.name)
        val email = view.findViewById<TextView>(R.id.email)
        val edit = view.findViewById<Button>(R.id.btnEdit)
        val delete = view.findViewById<Button>(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(v)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.name.text = user.fullName
        holder.email.text = user.email
        holder.edit.setOnClickListener { onEdit(user) }
        holder.delete.setOnClickListener { onDelete(user) }
    }
}