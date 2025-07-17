package com.example.tutionmanager

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity: AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().getReference("users")

        val spinner = findViewById<Spinner>(R.id.roleSpinner)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listOf("admin", "teacher", "student"))

        findViewById<Button>(R.id.registerBtn).setOnClickListener {
            val name = findViewById<EditText>(R.id.name).text.toString()
            val email = findViewById<EditText>(R.id.email).text.toString()
            val pass = findViewById<EditText>(R.id.password).text.toString()
            val role = spinner.selectedItem.toString()

            auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                if (it.isSuccessful) {
                    val uid = auth.currentUser!!.uid
                    val user = User(uid, name, email, role)
                    dbRef.child(uid).setValue(user).addOnCompleteListener {
                        Toast.makeText(this, "Registered!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                    }
                }
            }
        }
    }
}