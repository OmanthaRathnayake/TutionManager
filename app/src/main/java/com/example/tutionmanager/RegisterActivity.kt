package com.example.tutionmanager

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import com.example.tutionmanager.QRCodeUtils


class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().getReference("users")

        val spinner = findViewById<AutoCompleteTextView>(R.id.roleSpinner)
        spinner.setAdapter(ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, listOf("admin", "teacher", "student")))


        findViewById<Button>(R.id.registerBtn).setOnClickListener {
            val name = findViewById<EditText>(R.id.name).text.toString()
            val email = findViewById<EditText>(R.id.email).text.toString()
            val pass = findViewById<EditText>(R.id.password).text.toString()
            val role = spinner.text.toString()


            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener {
                if (it.isSuccessful) {
                    val uid = auth.currentUser!!.uid
                    val qrBitmap = QRCodeUtils.generateQRCode(uid)

                    if (qrBitmap != null) {
                        uploadQRCodeToFirebase(uid, name, email, role, qrBitmap)
                    } else {
                        val user = User(uid, name, email, role, "")
                        saveUserToDatabase(uid, user)
                    }
                } else {
                    Toast.makeText(this, "Registration Failed: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadQRCodeToFirebase(uid: String, name: String, email: String, role: String, bitmap: Bitmap) {
        val storageRef = FirebaseStorage.getInstance().getReference("qrcodes/$uid.png")

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()

        storageRef.putBytes(data)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    val user = User(uid, name, email, role, uri.toString())
                    saveUserToDatabase(uid, user)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "QR Code upload failed", Toast.LENGTH_SHORT).show()
                val user = User(uid, name, email, role, "")
                saveUserToDatabase(uid, user)
            }
    }

    private fun saveUserToDatabase(uid: String, user: User) {
        dbRef.child(uid).setValue(user).addOnCompleteListener {
            if (it.isSuccessful) {
                Toast.makeText(this, "Registered!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Failed to save user", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


