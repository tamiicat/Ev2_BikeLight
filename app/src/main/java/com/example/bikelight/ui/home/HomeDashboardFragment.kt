package com.example.bikelight.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.content.Intent
import com.example.bikelight.MainActivity
import com.example.bikelight.ui.home.WifiConfigActivity
import com.example.bikelight.ui.home.BuzzerActivity // 1. Import de BuzzerActivity
import com.example.bikelight.R
import com.example.bikelight.databinding.FragmentHomeDashboardBinding

class HomeDashboardFragment : Fragment() {

    private var _binding: FragmentHomeDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mainActivity = activity as? MainActivity

        binding.cardEstadoWifi.setOnClickListener {
            mainActivity?.cargarFragment(EstadoWifiFragment(), "Estado WiFi")
        }

        binding.cardBicicleta.setOnClickListener {
            mainActivity?.cargarFragment(BicicletaFragment(), "Mi Bicicleta")
        }

        binding.cardModoProximidad.setOnClickListener {
            mainActivity?.cargarFragment(ModoProximidadFragment(), "Modo Proximidad")
        }

        binding.cardSos.setOnClickListener {
            Toast.makeText(requireContext(), "EMERGENCIA ACTIVADA", Toast.LENGTH_SHORT).show()
        }

        binding.cardConfigWifi.setOnClickListener {
            val intent = Intent(requireContext(), WifiConfigActivity::class.java)
            startActivity(intent)
        }

        binding.cardConfigBuzzer.setOnClickListener {
            val intent = Intent(requireContext(), BuzzerActivity::class.java)
            startActivity(intent)
        }

        binding.cardConfigLed.setOnClickListener {
            Toast.makeText(requireContext(), "Configuración LED", Toast.LENGTH_SHORT).show()
        }
        binding.cardPreferencias.setOnClickListener {
            Toast.makeText(requireContext(), "Preferencias", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}