package com.example.bikelight.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bikelight.data.local.AppDatabase
import com.example.bikelight.data.local.UserProfile
import com.example.bikelight.databinding.FragmentBicicletaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BicicletaFragment : Fragment() {

    private var _binding: FragmentBicicletaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBicicletaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cargarDatosBicicleta()

        binding.btnGuardarBici.setOnClickListener {
            guardarDatosBicicleta()
        }
    }

    private fun valorOValorPorDefecto(valor: String?, porDefecto: String): String {
        return if (valor.isNullOrBlank()) porDefecto else valor
    }

    private fun cargarDatosBicicleta() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val safeContext = context ?: return

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(safeContext)
            val user = db.userDao().getUserProfile(uid)

            withContext(Dispatchers.Main) {
                user?.let { pintarFichaYCampos(it) }
            }

            // Sincronizar desde Firebase Realtime Database
            FirebaseDatabase.getInstance().reference.child("users").child(uid).get()
                .addOnSuccessListener { snapshot ->
                    val userFirebase = snapshot.getValue(UserProfile::class.java)
                    userFirebase?.let {
                        pintarFichaYCampos(it)
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            db.userDao().saveUserProfile(it)
                        }
                    }
                }
        }
    }

    private fun pintarFichaYCampos(user: UserProfile) {
        if (_binding == null) return

        // Actualizar tarjeta superior (convirtiendo bikeAno a String)
        binding.tvDisplayTipo.text = "Tipo: ${valorOValorPorDefecto(user.bikeTipo, "No registrado")}"
        binding.tvDisplayMarca.text = "Marca: ${valorOValorPorDefecto(user.bikeMarca, "No registrada")}"
        binding.tvDisplayModelo.text = "Modelo: ${valorOValorPorDefecto(user.bikeModelo, "No registrado")}"
        binding.tvDisplayAno.text = "Año: ${valorOValorPorDefecto(user.bikeAno?.toString(), "No registrado")}"
        binding.tvDisplayColor.text = "Color: ${valorOValorPorDefecto(user.bikeColor, "No registrado")}"

        // Rellenar campos del formulario (convirtiendo bikeAno a String para setText)
        binding.etBikeTipo.setText(user.bikeTipo ?: "")
        binding.etBikeMarca.setText(user.bikeMarca ?: "")
        binding.etBikeModelo.setText(user.bikeModelo ?: "")
        binding.etBikeAno.setText(user.bikeAno?.toString() ?: "")
        binding.etBikeColor.setText(user.bikeColor ?: "")
    }
    private fun guardarDatosBicicleta() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val safeContext = context ?: return

        val tipo = binding.etBikeTipo.text.toString().trim()
        val marca = binding.etBikeMarca.text.toString().trim()
        val modelo = binding.etBikeModelo.text.toString().trim()
        val ano = binding.etBikeAno.text.toString().trim()
        val color = binding.etBikeColor.text.toString().trim()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(safeContext)
            db.userDao().updateBikeInfo(uid, tipo, marca, modelo, ano, color)

            val bikeMap = mapOf(
                "bikeTipo" to tipo,
                "bikeMarca" to marca,
                "bikeModelo" to modelo,
                "bikeAno" to ano,
                "bikeColor" to color
            )
            FirebaseDatabase.getInstance().reference.child("users").child(uid).updateChildren(bikeMap)

            withContext(Dispatchers.Main) {
                if (_binding != null) {
                    binding.tvDisplayTipo.text = "Tipo: ${valorOValorPorDefecto(tipo, "No registrado")}"
                    binding.tvDisplayMarca.text = "Marca: ${valorOValorPorDefecto(marca, "No registrada")}"
                    binding.tvDisplayModelo.text = "Modelo: ${valorOValorPorDefecto(modelo, "No registrado")}"
                    binding.tvDisplayAno.text = "Año: ${valorOValorPorDefecto(ano, "No registrado")}"
                    binding.tvDisplayColor.text = "Color: ${valorOValorPorDefecto(color, "No registrado")}"
                }
                Toast.makeText(safeContext, "Información guardada exitosamente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}