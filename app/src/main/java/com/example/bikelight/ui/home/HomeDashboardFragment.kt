package com.example.bikelight.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bikelight.MainActivity
import com.example.bikelight.databinding.FragmentHomeDashboardBinding
import com.example.bikelight.ui.settings.PreferenciasFragment
import com.example.bikelight.ui.settings.SosSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeDashboardFragment : Fragment() {
    // solicitar permisos de ubicación
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val concedido = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (concedido) {
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                SosSender.dispararAlertaSos(requireContext())
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Sin permiso de GPS se enviará la alerta sin coordenadas exactas",
                Toast.LENGTH_SHORT
            ).show()
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                SosSender.dispararAlertaSos(requireContext())
            }
        }
    }

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

        binding.cardConfigWifi.setOnClickListener {
            mainActivity?.cargarFragment(WifiConfigFragment(), "Configuración Wi-Fi")
        }

        binding.cardBicicleta.setOnClickListener {
            mainActivity?.cargarFragment(BicicletaFragment(), "Mi Bicicleta")
        }

        binding.cardModoProximidad.setOnClickListener {
            mainActivity?.cargarFragment(ModoProximidadFragment(), "Modo Proximidad")
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

        // mensaje sos
        binding.cardContactoEmergencia.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("¡ALERTA DE EMERGENCIA!")
                .setMessage("¿Deseas enviar inmediatamente el mensaje SOS a tu contacto de emergencia con tu ubicación?")
                .setPositiveButton("ENVIAR SOS") { _, _ ->
                    val tienePermisoFine = ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    val tienePermisoCoarse = ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (!tienePermisoFine && !tienePermisoCoarse) {
                        // solicita permiso antes de enviar
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    } else {
                        // enviar msj
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                            SosSender.dispararAlertaSos(requireContext())
                        }
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}