package com.example.bikelight.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bikelight.data.local.AppDatabase
import com.example.bikelight.data.local.UserProfile
import com.example.bikelight.databinding.FragmentPerfilBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PerfilFragment : Fragment() {

    private var _binding: FragmentPerfilBinding? = null
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPerfilBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargarDatos()
    }

    private fun cargarDatos() {
        val userAuth = FirebaseAuth.getInstance().currentUser ?: return
        val uid = userAuth.uid
        val safeContext = context ?: return

        // 1. Mostrar de inmediato el correo de la sesión activa
        binding?.let { b ->
            b.tvProfileEmail.text = userAuth.email ?: "Sin correo"
        }

        // 2. Consulta en segundo plano en Room y Firebase
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val db = AppDatabase.getDatabase(safeContext)
                val userLocal = db.userDao().getUserProfile(uid)

                withContext(Dispatchers.Main) {
                    userLocal?.let { pintarDatos(it) }
                }

                // Consulta a Firebase Realtime Database
                FirebaseDatabase.getInstance().reference.child("users").child(uid).get()
                    .addOnSuccessListener { snapshot ->
                        try {
                            val userFirebase = snapshot.getValue(UserProfile::class.java)
                            if (userFirebase != null) {
                                pintarDatos(userFirebase)
                                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                    db.userDao().saveUserProfile(userFirebase)
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("PerfilFragment", "Error al parsear datos de Firebase: ${e.message}")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("PerfilFragment", "Error al leer de Firebase: ${e.message}")
                    }
            } catch (e: Exception) {
                Log.e("PerfilFragment", "Error en base de datos local: ${e.message}")
            }
        }
    }

    private fun pintarDatos(user: UserProfile) {
        binding?.let { b ->
            b.tvProfileName.text = if (!user.fullName.isNullOrBlank()) user.fullName else "No registrado"
            b.tvProfileUser.text = when {
                !user.username.isNullOrBlank() -> user.username
                !user.email.isNullOrBlank() -> user.email.substringBefore("@")
                else -> "Usuario"
            }
            b.tvProfileEmail.text = if (!user.email.isNullOrBlank()) user.email else "Sin correo"
            b.tvProfilePhone.text = if (!user.phone.isNullOrBlank()) user.phone else "No registrado"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}