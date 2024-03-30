package com.nav.firebaseApp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.nav.firebaseApp.databinding.ActivityRegistrationBinding

class RegistrationActivity : AppCompatActivity() {
    private var userName = ""
    private var email = ""
    private var password = ""
    private lateinit var binding: ActivityRegistrationBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.signupBtn.setOnClickListener {
            signUp()
        }

    }

    private fun signUp() {
        userName = binding.userNameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        if (userName.isEmpty()) {
            Toast.makeText(this,"User name cannot be empty", Toast.LENGTH_SHORT).show()
            return
        } else if(email.isEmpty()) {
            Toast.makeText(this,"Email cannot be empty", Toast.LENGTH_SHORT).show()
            return
        } else if (password.isEmpty()) {
            Toast.makeText(this,"Password cannot be empty", Toast.LENGTH_SHORT).show()
            return
        } else {
            binding.signupBtn.text = getString(R.string.loading)
            binding.signupBtn.alpha = 0.5f
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if(task.isSuccessful) {

                        val user = auth.currentUser
                        val uid = user?.uid
                        val userMap = hashMapOf(
                            "username" to userName,
                            "email" to email
                        )

                        firestore.collection("users").document(uid!!).set(userMap)
                            .addOnCompleteListener {
                                Toast.makeText(this,"You have signed up successfully!", Toast.LENGTH_SHORT).show()
                                goToProfile()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Failed to create user profile: ${e.message}", Toast.LENGTH_LONG).show()
                            }

                    } else {
                        Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
        binding.signupBtn.text = getString(R.string.sign_up)
        binding.signupBtn.alpha = 1f

    }

    private fun goToProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
        this.overridePendingTransition(R.anim.right_in, R.anim.left_out)
        finish()
    }
}