package com.example.bikelight.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bikelight.databinding.FragmentAhorroEnergiaBinding

class AhorroEnergiaFragment : Fragment() {

    private var _binding: FragmentAhorroEnergiaBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAhorroEnergiaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cargarPreferencias()

        // control padre: si se desactiva el principal, se desactivan los secundarios
        binding.switchAhorroGeneral.setOnCheckedChangeListener { _, isChecked ->
            actualizarEstadoVisual(isChecked)
        }

        binding.btnGuardarAhorro.setOnClickListener {
            guardarPreferencias()
        }
    }

    private fun actualizarEstadoVisual(activo: Boolean) {
        binding.tvEstadoGeneralAhorro.text = if (activo) "Activado" else "Desactivado"
        binding.switchReducirBrillo.isEnabled = activo
        binding.switchDesactivarAnimaciones.isEnabled = activo
        binding.switchAlertaDespertar.isEnabled = activo
    }

    private fun cargarPreferencias() {
        val prefs = requireContext().getSharedPreferences("BikeLightPrefs", Context.MODE_PRIVATE)
        val general = prefs.getBoolean("ahorro_general", false)
        val brillo = prefs.getBoolean("ahorro_reducir_brillo", true)
        val animaciones = prefs.getBoolean("ahorro_desactivar_animaciones", true)
        val alertaDespertar = prefs.getBoolean("ahorro_alerta_despertar", false)

        binding.switchAhorroGeneral.isChecked = general
        binding.switchReducirBrillo.isChecked = brillo
        binding.switchDesactivarAnimaciones.isChecked = animaciones
        binding.switchAlertaDespertar.isChecked = alertaDespertar

        actualizarEstadoVisual(general)
    }

    private fun guardarPreferencias() {
        val prefs = requireContext().getSharedPreferences("BikeLightPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("ahorro_general", binding.switchAhorroGeneral.isChecked)
            putBoolean("ahorro_reducir_brillo", binding.switchReducirBrillo.isChecked)
            putBoolean("ahorro_desactivar_animaciones", binding.switchDesactivarAnimaciones.isChecked)
            putBoolean("ahorro_alerta_despertar", binding.switchAlertaDespertar.isChecked)
            apply()
        }

        Toast.makeText(requireContext(), "Ajustes de ahorro de energía guardados", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}