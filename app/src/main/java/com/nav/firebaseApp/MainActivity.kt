package com.nav.firebaseApp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.nav.firebaseApp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEmail.setOnClickListener {
            register()
        }
    }

    private fun register() {
        val intent = Intent(this, RegistrationActivity::class.java)
        startActivity(intent)
        this.overridePendingTransition(R.anim.right_in, R.anim.left_out)
        finish()
    }
}