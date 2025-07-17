package com.example.tutionmanager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class UsersFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var addUserBtn: Button
    private lateinit var btnStudents: Button
    private lateinit var btnTeachers: Button

    private val userList = mutableListOf<User>()
    private lateinit var adapter: UserAdapter
    private val dbRef = FirebaseDatabase.getInstance().getReference("users")

    private var currentRole = "student" // default load

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.fragment_users, container, false)
        recyclerView = view.findViewById(R.id.recyclerUsers)
        addUserBtn = view.findViewById(R.id.btnAddUser)
        btnStudents = view.findViewById(R.id.btnStudents)
        btnTeachers = view.findViewById(R.id.btnTeachers)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = UserAdapter(userList, ::showUpdateDialog, ::deleteUser)
        recyclerView.adapter = adapter

        loadUsers()

        btnStudents.setOnClickListener {
            currentRole = "student"
            loadUsers()
        }

        btnTeachers.setOnClickListener {
            currentRole = "teacher"
            loadUsers()
        }

        addUserBtn.setOnClickListener {
            showAddDialog()
        }

        return view
    }

    private fun loadUsers() {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (child in snapshot.children) {
                    val user = child.getValue(User::class.java)
                    if (user != null && user.role == currentRole) {
                        userList.add(user)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load users", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_user, null)
        val etName = dialogView.findViewById<EditText>(R.id.name)
        val etEmail = dialogView.findViewById<EditText>(R.id.email)
        val etPassword = dialogView.findViewById<EditText>(R.id.password)

        AlertDialog.Builder(requireContext())
            .setTitle("Add $currentRole")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString()
                val email = etEmail.text.toString()
                val pass = etPassword.text.toString()

                FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, pass)
                    .addOnSuccessListener {
                        val uid = it.user!!.uid
                        val user = User(uid, name, email, currentRole)
                        dbRef.child(uid).setValue(user).addOnSuccessListener {
                            Toast.makeText(requireContext(), "User added!", Toast.LENGTH_SHORT).show()
                            loadUsers()
                        }
                    }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showUpdateDialog(user: User) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_user, null)
        val etName = dialogView.findViewById<EditText>(R.id.name)
        val etEmail = dialogView.findViewById<EditText>(R.id.email)
        val etPassword = dialogView.findViewById<EditText>(R.id.password)

        etName.setText(user.fullName)
        etEmail.setText(user.email)
        etEmail.isEnabled = false // email cannot be changed
        etPassword.visibility = View.GONE // no password update here

        AlertDialog.Builder(requireContext())
            .setTitle("Update ${user.role}")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val updatedUser = User(user.uid, etName.text.toString(), user.email, user.role)
                dbRef.child(user.uid).setValue(updatedUser).addOnSuccessListener {
                    Toast.makeText(requireContext(), "User updated!", Toast.LENGTH_SHORT).show()
                    loadUsers()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteUser(user: User) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete")
            .setMessage("Are you sure to delete ${user.fullName}?")
            .setPositiveButton("Yes") { _, _ ->
                dbRef.child(user.uid).removeValue().addOnSuccessListener {
                    Toast.makeText(requireContext(), "User deleted!", Toast.LENGTH_SHORT).show()
                    loadUsers()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }

    }

