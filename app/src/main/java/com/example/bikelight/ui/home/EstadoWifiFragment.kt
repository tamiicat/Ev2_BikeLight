package com.example.bikelight.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bikelight.R
import com.example.bikelight.databinding.FragmentEstadoWifiBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class EstadoWifiFragment : Fragment() {
    private var _binding: FragmentEstadoWifiBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEstadoWifiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivWifiIcon.setImageResource(R.drawable.wifi)

        // simulación de estados
        simularEstadosWifi()

        // Opcional: permitir al usuario tocar la imagen/pantalla para reiniciar la simulación
        binding.ivWifiIcon.setOnClickListener {
            simularEstadosWifi()
        }
    }

    private fun simularEstadosWifi() {
        viewLifecycleOwner.lifecycleScope.launch {
            // estado inicial (buscando...)
            binding.tvWifiStatusDetail.text = "Buscando dispositivo..."
            binding.tvWifiStatusDetail.setTextColor(Color.parseColor("#FFA500")) // Naranja
            binding.ivWifiIcon.alpha = 0.5f // Efecto visual atenuado

            delay(2500) // Simula 2.5 segundos de espera

            // estado conectado con el dispositivo
            binding.tvWifiStatusDetail.text = "Conectado al dispositivo"
            binding.tvWifiStatusDetail.setTextColor(Color.parseColor("#2E7D32")) // Verde
            binding.ivWifiIcon.alpha = 1.0f
            // imagen para cuando está conectado
            binding.ivWifiIcon.setImageResource(R.drawable.wifi_conectado)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}