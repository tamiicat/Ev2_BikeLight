package com.example.bikelight.ui.home

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bikelight.databinding.FragmentWifiConfigBinding

class WifiConfigFragment : Fragment() {

    private var _binding: FragmentWifiConfigBinding? = null
    private val binding get() = _binding!!

    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWifiConfigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnConnectDevice.setOnClickListener {
            val ssid = binding.etDeviceAp.text.toString().trim()
            val pass = binding.etDeviceApPass.text.toString().trim()

            if (ssid.isEmpty()) {
                binding.tilDeviceAp.error = "Ingresa el SSID del ESP32"
                return@setOnClickListener
            }
            binding.tilDeviceAp.error = null

            connectToEsp32Ap(ssid, pass)
        }
    }

    private fun connectToEsp32Ap(ssid: String, pass: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.progressBar.visibility = View.VISIBLE
            binding.btnConnectDevice.isEnabled = false

            unregisterNetworkCallback()

            val specifierBuilder = WifiNetworkSpecifier.Builder().setSsid(ssid)
            if (pass.isNotEmpty()) {
                specifierBuilder.setWpa2Passphrase(pass)
            }

            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .setNetworkSpecifier(specifierBuilder.build())
                .build()

            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    connectivityManager?.bindProcessToNetwork(network)
                    activity?.runOnUiThread {
                        _binding?.progressBar?.visibility = View.GONE
                        _binding?.btnConnectDevice?.isEnabled = true
                        Toast.makeText(requireContext(), "Conectado al punto de acceso $ssid", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    activity?.runOnUiThread {
                        _binding?.progressBar?.visibility = View.GONE
                        _binding?.btnConnectDevice?.isEnabled = true
                        Toast.makeText(requireContext(), "Conexión rechazada o no encontrada", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onLost(network: Network) {
                    super.onLost(network)
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(), "Conexión con el ESP32 finalizada", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            networkCallback?.let { connectivityManager?.requestNetwork(request, it) }
        } else {
            Toast.makeText(
                requireContext(),
                "En Android 9 o inferior, conéctate a '$ssid' manualmente desde Ajustes de Wi-Fi",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun unregisterNetworkCallback() {
        networkCallback?.let {
            try {
                connectivityManager?.unregisterNetworkCallback(it)
            } catch (_: Exception) {}
            networkCallback = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unregisterNetworkCallback()
        connectivityManager?.bindProcessToNetwork(null)
        _binding = null
    }
}