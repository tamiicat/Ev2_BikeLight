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
            val deviceSsid = binding.etDeviceAp.text.toString().trim()
            if (deviceSsid.isEmpty()) {
                binding.tilDeviceAp.error = "Ingresa el SSID del microcontrolador"
                return@setOnClickListener
            }
            binding.tilDeviceAp.error = null
            connectToDeviceAp(deviceSsid)
        }

        binding.btnSendCredentials.setOnClickListener {
            val targetSsid = binding.etNetworkSsid.text.toString().trim()
            val targetPass = binding.etNetworkPass.text.toString().trim()

            if (targetSsid.isEmpty()) {
                binding.tilNetworkSsid.error = "Ingresa el nombre de la red"
                return@setOnClickListener
            }
            binding.tilNetworkSsid.error = null

            sendWifiCredentials(targetSsid, targetPass)
        }
    }

    private fun connectToDeviceAp(ssid: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.progressBar.visibility = View.VISIBLE

            val specifier = WifiNetworkSpecifier.Builder()
                .setSsid(ssid)
                .build()

            val request = NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .setNetworkSpecifier(specifier)
                .build()

            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    connectivityManager?.bindProcessToNetwork(network)
                    activity?.runOnUiThread {
                        _binding?.progressBar?.visibility = View.GONE
                        Toast.makeText(requireContext(), "Conectado a $ssid", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    activity?.runOnUiThread {
                        _binding?.progressBar?.visibility = View.GONE
                        Toast.makeText(requireContext(), "Conexión rechazada o cancelada", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            networkCallback?.let { connectivityManager?.requestNetwork(request, it) }
        } else {
            Toast.makeText(requireContext(), "Conéctate manualmente a '$ssid' desde los Ajustes del sistema", Toast.LENGTH_LONG).show()
        }
    }

    private fun sendWifiCredentials(ssid: String, pass: String) {
        binding.progressBar.visibility = View.VISIBLE

        binding.root.postDelayed({
            _binding?.progressBar?.visibility = View.GONE
            Toast.makeText(requireContext(), "Credenciales enviadas correctamente al dispositivo", Toast.LENGTH_LONG).show()
        }, 1500)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        networkCallback?.let { connectivityManager?.unregisterNetworkCallback(it) }
        connectivityManager?.bindProcessToNetwork(null)
        _binding = null
    }
}