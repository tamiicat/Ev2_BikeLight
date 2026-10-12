package com.example.bikelight.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bikelight.MainActivity
import com.example.bikelight.ui.settings.PreferenciasFragment
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

        binding.cardContactoEmergencia.setOnClickListener {
            mainActivity?.cargarFragment(EmergencyContactFragment(), "Contacto de Emergencia")
        }

        binding.cardConfigWifi.setOnClickListener {
            mainActivity?.cargarFragment(WifiConfigFragment(), "Configuración Wi-Fi")
        }

        binding.cardConfigBuzzer.setOnClickListener {
            mainActivity?.cargarFragment(BuzzerFragment(), "Configuración Buzzer")
        }

        binding.cardConfigLed.setOnClickListener {
            mainActivity?.cargarFragment(LedConfigFragment(), "Configuración LED")
        }

        binding.cardPreferencias.setOnClickListener {
            mainActivity?.cargarFragment(PreferenciasFragment(), "Preferencias")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}