package com.example.bikelight.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bikelight.MainActivity
import com.example.bikelight.databinding.ActivityAutenticacionBinding
import com.google.firebase.auth.FirebaseAuth

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAutenticacionBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAutenticacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Comprobar si ya existe una sesión iniciada
        if (auth.currentUser != null) {
            irAMain()
            return
        }

        // Ocultar campos innecesarios en la vista si reutilizas el layout
        binding.tilFullName.visibility = View.GONE
        binding.tilUsername.visibility = View.GONE
        binding.tilPhone.visibility = View.GONE
        binding.btnAction.text = "Iniciar Sesión"
        binding.tvSwitchMode.text = "¿No tienes cuenta? Regístrate aquí"

        // Iniciar sesión
        binding.btnAction.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (email.isEmpty()) {
                binding.etEmail.error = "Ingresa tu correo"
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }
            if (pass.isEmpty()) {
                binding.etPassword.error = "Ingresa tu contraseña"
                binding.etPassword.requestFocus()
                return@setOnClickListener
            }
            iniciarSesion(email, pass)
        }

        // Ir a la pantalla de Registro
        binding.tvSwitchMode.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun iniciarSesion(email: String, pass: String) {
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "¡Bienvenido! Sesión iniciada con éxito", Toast.LENGTH_SHORT).show()
                irAMain()
            } else {
                val errorMsg = task.exception?.localizedMessage ?: "Error de autenticación"
                Toast.makeText(this, "Error al iniciar sesión: $errorMsg", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun irAMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}