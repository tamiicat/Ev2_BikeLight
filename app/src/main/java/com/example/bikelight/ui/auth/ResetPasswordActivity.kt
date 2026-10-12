package com.example.bikelight.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bikelight.databinding.ActivityResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnSendResetEmail.setOnClickListener {
            val email = binding.etResetEmail.text.toString().trim()

            if (email.isEmpty()) {
                binding.etResetEmail.error = "Ingresa tu correo"
                binding.etResetEmail.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.etResetEmail.error = "Ingresa un correo electrónico válido"
                binding.etResetEmail.requestFocus()
                return@setOnClickListener
            }

            enviarCorreoRecuperacion(email)
        }

        binding.btnBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun enviarCorreoRecuperacion(email: String) {
        binding.btnSendResetEmail.isEnabled = false

        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
            binding.btnSendResetEmail.isEnabled = true
            if (task.isSuccessful) {
                Toast.makeText(
                    this,
                    "Enlace enviado a $email. Revisa tu bandeja de entrada o spam.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            } else {
                val errorMsg = task.exception?.localizedMessage ?: "No se pudo enviar el correo"
                Toast.makeText(this, "Error: $errorMsg", Toast.LENGTH_LONG).show()
            }
        }
    }
}