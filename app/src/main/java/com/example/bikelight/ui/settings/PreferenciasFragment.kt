package com.example.bikelight.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.bikelight.MainActivity
import com.example.bikelight.databinding.FragmentPreferenciasBinding

class PreferenciasFragment : Fragment() {

    private var _binding: FragmentPreferenciasBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPreferenciasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // tema aplicación
        binding.cardTemaApp.setOnClickListener {
            mostrarDialogoTema()
        }

        // configuración unidades de medidas
        binding.cardUnidadesMedida.setOnClickListener {
            (activity as? MainActivity)?.cargarFragment(UnidadesMedidaFragment(), "Unidades de Medida")
        }

        // modo ahorro energia
        binding.cardAhorroEnergia.setOnClickListener {
            mostrarDialogoAhorroEnergia()
        }

        // configuración msj SOS
        binding.cardConfigMensajeSos.setOnClickListener {
            mostrarDialogoMensajeSos()
        }

        // borrar caché
        binding.cardBorrarCache.setOnClickListener {
            confirmarBorradoCache()
        }
    }

    private fun mostrarDialogoTema() {
        val opciones = arrayOf("Modo Claro", "Modo Oscuro", "Predeterminado del sistema")
        AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar Tema")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
                Toast.makeText(requireContext(), "Tema aplicado: ${opciones[which]}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoUnidades() {
        val unidades = arrayOf("Métrico (km/h, metros)", "Imperial (mph, pies)")
        AlertDialog.Builder(requireContext())
            .setTitle("Unidades de Medida")
            .setItems(unidades) { _, which ->
                Toast.makeText(requireContext(), "Seleccionado: ${unidades[which]}", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun mostrarDialogoAhorroEnergia() {
        AlertDialog.Builder(requireContext())
            .setTitle("Modo Ahorro de Energía")
            .setMessage("Al activarlo, se atenuará el brillo de la pantalla durante el trayecto y se desactivarán animaciones secundarias.")
            .setPositiveButton("Activar") { _, _ ->
                Toast.makeText(requireContext(), "Ahorro de energía activado", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Desactivar") { _, _ ->
                Toast.makeText(requireContext(), "Ahorro de energía desactivado", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun mostrarDialogoMensajeSos() {
        val input = EditText(requireContext()).apply {
            hint = "¡Emergencia! He tenido un incidente en bicicleta."
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Mensaje SOS")
            .setMessage("Personaliza el mensaje que se enviará a tu contacto de emergencia:")
            .setView(input)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevoMensaje = input.text.toString().trim()
                if (nuevoMensaje.isNotEmpty()) {
                    Toast.makeText(requireContext(), "Mensaje SOS actualizado", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarBorradoCache() {
        AlertDialog.Builder(requireContext())
            .setTitle("Borrar Caché")
            .setMessage("¿Deseas eliminar los datos temporales de la app? Tus configuraciones principales no se perderán.")
            .setPositiveButton("Borrar") { _, _ ->
                try {
                    requireContext().cacheDir.deleteRecursively()
                    Toast.makeText(requireContext(), "Caché eliminada con éxito", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error al limpiar caché", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}