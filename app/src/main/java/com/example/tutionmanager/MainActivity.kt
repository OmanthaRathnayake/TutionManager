package com.example.tutionmanager

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val dbRef = FirebaseDatabase.getInstance().getReference("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Check if user is already logged in
        val user = auth.currentUser
        if (user != null) {
            dbRef.child(user.uid).get().addOnSuccessListener { snapshot ->
                val role = snapshot.child("role").value.toString()
                when (role) {
                    "admin" -> startActivity(Intent(this, AdminDashboardActivity::class.java))
                    "teacher" -> startActivity(Intent(this, TeacherDashboardActivity::class.java))
                    "student" -> startActivity(Intent(this, StudentDashboardActivity::class.java))
                }
                finish()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to load role", Toast.LENGTH_SHORT).show()
            }
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

    }
}