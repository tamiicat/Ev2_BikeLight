package com.example.bikelight.ui.home

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bikelight.data.local.AppDatabase
import com.example.bikelight.data.local.UserProfile
import com.example.bikelight.databinding.FragmentEmergencyContactBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EmergencyContactFragment : Fragment() {

    private var _binding: FragmentEmergencyContactBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEmergencyContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargarDatosContacto()

        binding.btnSaveContact.setOnClickListener {
            guardarDatosContacto()
        }
    }

    private fun valorOValorPorDefecto(valor: String?, porDefecto: String): String {
        return if (valor.isNullOrBlank()) porDefecto else valor
    }

    private fun cargarDatosContacto() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val safeContext = context ?: return

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(safeContext)
            val user = db.userDao().getUserProfile(uid)

            withContext(Dispatchers.Main) {
                user?.let { pintarFichaYCampos(it) }
            }

            // Sincronizar datos desde Firebase Realtime Database
            FirebaseDatabase.getInstance().reference.child("users").child(uid).get()
                .addOnSuccessListener { snapshot ->
                    try {
                        val userFirebase = snapshot.getValue(UserProfile::class.java)
                        userFirebase?.let { profile ->
                            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                                pintarFichaYCampos(profile)
                            }
                            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                db.userDao().saveUserProfile(profile)
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                .addOnFailureListener {
                }
        }
    }

    private fun pintarFichaYCampos(user: UserProfile) {
        if (_binding == null) return

        // Actualizar tarjeta superior
        binding.tvDisplayContactName.text = "Nombre: ${valorOValorPorDefecto(user.emergencyContactName, "No registrado")}"
        binding.tvDisplayContactPhone.text = "Teléfono: ${valorOValorPorDefecto(user.emergencyContactPhone, "No registrado")}"
        binding.tvDisplayContactEmail.text = "Correo: ${valorOValorPorDefecto(user.emergencyContactEmail, "No registrado")}"

        // Rellenar campos del formulario
        binding.etEmergencyContactName.setText(user.emergencyContactName ?: "")
        binding.etEmergencyContactPhone.setText(user.emergencyContactPhone ?: "")
        binding.etEmergencyContactEmail.setText(user.emergencyContactEmail ?: "")
    }

    private fun guardarDatosContacto() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val safeContext = context ?: return

        val name = binding.etEmergencyContactName.text.toString().trim()
        val phone = binding.etEmergencyContactPhone.text.toString().trim()
        val email = binding.etEmergencyContactEmail.text.toString().trim()

        if (name.isEmpty()) {
            Toast.makeText(safeContext, "Por favor ingresa un nombre", Toast.LENGTH_SHORT).show()
            return
        }

        if (phone.isEmpty()) {
            Toast.makeText(safeContext, "Por favor ingresa un teléfono", Toast.LENGTH_SHORT).show()
            return
        }

        if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(safeContext, "El formato de correo no es válido", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(safeContext)

            // Actualización local directa en Room usando UserDao
            db.userDao().updateEmergencyContact(uid, name, phone, email)

            // Actualización en Firebase Realtime Database
            val contactMap: Map<String, Any?> = mapOf(
                "emergencyContactName" to name,
                "emergencyContactPhone" to phone,
                "emergencyContactEmail" to email
            )

            FirebaseDatabase.getInstance().reference.child("users").child(uid)
                .updateChildren(contactMap)
                .addOnSuccessListener {
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                        if (_binding != null) {
                            binding.tvDisplayContactName.text = "Nombre: ${valorOValorPorDefecto(name, "No registrado")}"
                            binding.tvDisplayContactPhone.text = "Teléfono: ${valorOValorPorDefecto(phone, "No registrado")}"
                            binding.tvDisplayContactEmail.text = "Correo: ${valorOValorPorDefecto(email, "No registrado")}"
                            Toast.makeText(safeContext, "Contacto guardado exitosamente", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .addOnFailureListener {
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(safeContext, "Error al guardar en el servidor", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}