package com.example.tutionmanager

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().getReference("users")

        findViewById<Button>(R.id.loginbtn).setOnClickListener {
            val email = findViewById<EditText>(R.id.email).text.toString()
            val password = findViewById<EditText>(R.id.password).text.toString()

            auth.signInWithEmailAndPassword(email, password).addOnSuccessListener {
                val uid = auth.currentUser!!.uid

                dbRef.child(uid).get().addOnSuccessListener { snapshot ->
                    val role = snapshot.child("role").value.toString()
                    Toast.makeText(this, "Logged in as $role", Toast.LENGTH_SHORT).show()

                    when (role) {
                        "admin" -> startActivity(Intent(this, AdminDashboardActivity::class.java))
                        "teacher" -> startActivity(Intent(this, TeacherDashboardActivity::class.java))
                        "student" -> startActivity(Intent(this, StudentDashboardActivity::class.java))
                    }


                }
            }
        }

        findViewById<TextView>(R.id.registerLink).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}