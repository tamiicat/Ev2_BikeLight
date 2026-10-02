package com.example.bikelight.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bikelight.MainActivity
import com.example.bikelight.data.local.AppDatabase
import com.example.bikelight.data.local.UserProfile
import com.example.bikelight.databinding.ActivityAutenticacionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAutenticacionBinding
    private lateinit var auth: FirebaseAuth
    private val dbRef = FirebaseDatabase.getInstance().reference
    private lateinit var localDb: AppDatabase
    private var isLoginMode = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAutenticacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        localDb = AppDatabase.getDatabase(this)

        // Comprobar si ya existe una sesión iniciada
        if (auth.currentUser != null) {
            irAMain()
            return
        }

        configurarVistas()

        // Acción del botón principal (Iniciar Sesión o Registro)
        binding.btnAction.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            if (isLoginMode) {
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
            } else {
                val fullName = binding.etFullName.text.toString().trim()
                val username = binding.etUsername.text.toString().trim()
                val phone = binding.etPhone.text.toString().trim()

                if (fullName.isEmpty()) {
                    binding.etFullName.error = "El nombre completo es obligatorio"
                    binding.etFullName.requestFocus()
                    return@setOnClickListener
                }
                if (username.isEmpty()) {
                    binding.etUsername.error = "El nombre de usuario es obligatorio"
                    binding.etUsername.requestFocus()
                    return@setOnClickListener
                }
                if (email.isEmpty()) {
                    binding.etEmail.error = "El correo electrónico es obligatorio"
                    binding.etEmail.requestFocus()
                    return@setOnClickListener
                }
                if (phone.isEmpty()) {
                    binding.etPhone.error = "El teléfono es obligatorio"
                    binding.etPhone.requestFocus()
                    return@setOnClickListener
                }
                if (pass.isEmpty() || pass.length < 6) {
                    binding.etPassword.error = "La contraseña debe tener mínimo 6 caracteres"
                    binding.etPassword.requestFocus()
                    return@setOnClickListener
                }

                registrarUsuario(email, pass, fullName, username, phone)
            }
        }

        binding.tvSwitchMode.setOnClickListener {
            isLoginMode = !isLoginMode
            actualizarModo()
        }
    }

    private fun configurarVistas() {
        actualizarModo()
    }

    private fun actualizarModo() {
        if (isLoginMode) {
            binding.tilFullName.visibility = View.GONE
            binding.tilUsername.visibility = View.GONE
            binding.tilPhone.visibility = View.GONE
            binding.btnAction.text = "Iniciar Sesión"
            binding.tvSwitchMode.text = "¿No tienes cuenta? Regístrate aquí"
        } else {
            binding.tilFullName.visibility = View.VISIBLE
            binding.tilUsername.visibility = View.VISIBLE
            binding.tilPhone.visibility = View.VISIBLE
            binding.btnAction.text = "Registrarse"
            binding.tvSwitchMode.text = "¿Ya tienes cuenta? Inicia sesión"
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

    private fun registrarUsuario(
        email: String,
        pass: String,
        fullName: String,
        username: String,
        phone: String
    ) {
        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                val user = UserProfile(
                    uid = uid,
                    email = email,
                    fullName = fullName,
                    username = username,
                    phone = phone
                )

                // 1. Guardar en Firebase Realtime Database
                dbRef.child("users").child(uid).setValue(user).addOnCompleteListener { syncTask ->
                    if (syncTask.isSuccessful) {
                        // 2. Guardar en Room (SQLite local)
                        lifecycleScope.launch(Dispatchers.IO) {
                            localDb.userDao().saveUserProfile(user)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@AuthActivity, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show()
                                irAMain()
                            }
                        }
                    } else {
                        Toast.makeText(
                            this@AuthActivity,
                            "Usuario creado con aviso: ${syncTask.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } else {
                val errorMsg = task.exception?.localizedMessage ?: "No se pudo registrar"
                Toast.makeText(this, "Error al crear cuenta: $errorMsg", Toast.LENGTH_LONG).show()
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