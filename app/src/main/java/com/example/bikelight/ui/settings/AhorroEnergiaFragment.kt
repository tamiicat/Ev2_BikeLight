package com.example.bikelight.ui.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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

        // control padre: al cambiar el switch principal, habilita/deshabilita los secundarios
        binding.switchAhorroGeneral.setOnCheckedChangeListener { _, isChecked ->
            actualizarEstadoVisual(isChecked)
        }

        binding.btnGuardarAhorro.setOnClickListener {
            guardarYAplicarPreferencias()
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

        // aplicar estado
        aplicarEfectosReales(general, brillo, alertaDespertar)
    }

    private fun guardarYAplicarPreferencias() {
        val general = binding.switchAhorroGeneral.isChecked
        val brillo = binding.switchReducirBrillo.isChecked
        val animaciones = binding.switchDesactivarAnimaciones.isChecked
        val alertaDespertar = binding.switchAlertaDespertar.isChecked

        // guardar preferencias
        val prefs = requireContext().getSharedPreferences("BikeLightPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("ahorro_general", general)
            putBoolean("ahorro_reducir_brillo", brillo)
            putBoolean("ahorro_desactivar_animaciones", animaciones)
            putBoolean("ahorro_alerta_despertar", alertaDespertar)
            apply()
        }

        // aplicar cambios
        aplicarEfectosReales(general, brillo, alertaDespertar)

        Toast.makeText(requireContext(), "Ajustes aplicados en el dispositivo", Toast.LENGTH_SHORT).show()
    }

    private fun aplicarEfectosReales(general: Boolean, reducirBrillo: Boolean, soloAlerta: Boolean) {
        val window = activity?.window ?: return
        val layoutParams = window.attributes

        if (general) {
            // reducir brillo
            if (reducirBrillo) {
                layoutParams.screenBrightness = 0.15f
            } else {
                // brillo automático
                layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            }

            // encender pantalla por alerta
            if (soloAlerta) {
                // funcionamiento normal de pantalla
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                // pantalla encendida
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        } else {
            // restaurar valores normales
            layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        window.attributes = layoutParams
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}