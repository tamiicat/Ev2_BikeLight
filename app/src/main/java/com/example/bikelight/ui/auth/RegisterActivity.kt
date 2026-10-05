package com.example.bikelight.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bikelight.data.local.AppDatabase
import com.example.bikelight.data.local.UserProfile
import com.example.bikelight.databinding.ActivityAutenticacionBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAutenticacionBinding
    private lateinit var auth: FirebaseAuth
    private val dbRef = FirebaseDatabase.getInstance().reference
    private lateinit var localDb: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAutenticacionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        localDb = AppDatabase.getDatabase(this)

        // Configuración visual para registro
        binding.tilFullName.visibility = View.VISIBLE
        binding.tilUsername.visibility = View.VISIBLE
        binding.tilPhone.visibility = View.VISIBLE
        binding.btnAction.text = "Registrarse"
        binding.tvSwitchMode.text = "¿Ya tienes cuenta? Inicia sesión"

        // Botón para registrarse
        binding.btnAction.setOnClickListener {
            val fullName = binding.etFullName.text.toString().trim()
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

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
            if (pass.isEmpty() || pass.length < 8) {
                binding.etPassword.error = "La contraseña debe tener mínimo 8 caracteres"
                binding.etPassword.requestFocus()
                return@setOnClickListener
            }

            registrarUsuario(email, pass, fullName, username, phone)
        }

        // volver a login
        binding.tvSwitchMode.setOnClickListener {
            finish()
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

                // guardar5 en Firebase Realtime Database
                dbRef.child("users").child(uid).setValue(user).addOnCompleteListener { syncTask ->
                    if (syncTask.isSuccessful) {
                        // guardar en Room - SQLite local
                        lifecycleScope.launch(Dispatchers.IO) {
                            localDb.userDao().saveUserProfile(user)
                            withContext(Dispatchers.Main) {
                                auth.signOut()

                                Toast.makeText(
                                    this@RegisterActivity,
                                    "Cuenta creada exitosamente. Por favor, inicia sesión.",
                                    Toast.LENGTH_SHORT
                                ).show()

                                irAAuth()
                            }
                        }
                    } else {
                        Toast.makeText(
                            this@RegisterActivity,
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

    private fun irAAuth() {
        val intent = Intent(this, AuthActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}