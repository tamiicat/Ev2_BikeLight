package com.example.bikelight.ui.home

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.os.Bundle
import android.view.View
import android.content.Intent
import com.example.bikelight.R
import com.example.bikelight.MainActivity
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.bikelight.databinding.ActivityWifiConfigBinding

class WifiConfigActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWifiConfigBinding
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWifiConfigBinding.inflate(layoutInflater)
        setContentView(binding.root)

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        setupToolbar()
        setupListeners()
        setupBottomNavigation()
    }

    private fun setupToolbar() {
        binding.toolbarWifi.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupListeners() {
        // Conexión directa al AP del Microcontrolador
        binding.btnConnectDevice.setOnClickListener {
            val deviceSsid = binding.etDeviceAp.text.toString().trim()
            if (deviceSsid.isEmpty()) {
                binding.tilDeviceAp.error = "Ingresa el SSID del microcontrolador"
                return@setOnClickListener
            }
            binding.tilDeviceAp.error = null
            connectToDeviceAp(deviceSsid)
        }

        // Envío de credenciales Wi-Fi domésticas al microcontrolador
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
                    runOnUiThread {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this@WifiConfigActivity, "Conectado a $ssid", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onUnavailable() {
                    super.onUnavailable()
                    runOnUiThread {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(this@WifiConfigActivity, "Conexión rechazada o cancelada", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            networkCallback?.let { connectivityManager?.requestNetwork(request, it) }
        } else {
            Toast.makeText(this, "Conéctate manualmente a '$ssid' desde los Ajustes del sistema", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupBottomNavigation() {
        // 1. Evita que 'Perfil' quede preseleccionado si esta pantalla no es ninguna de las tres
        binding.bottomNavigationView.menu.setGroupCheckable(0, true, false)
        for (i in 0 until binding.bottomNavigationView.menu.size()) {
            binding.bottomNavigationView.menu.getItem(i).isChecked = false
        }
        binding.bottomNavigationView.menu.setGroupCheckable(0, true, true)

        // 2. Redirección de los botones del menú
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Vuelve a la pantalla principal
                    val intent = Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_perfil -> {
                    // Redirige a MainActivity cargando la sección de perfil o cierra para volver
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("NAVIGATE_TO", "PERFIL")
                    }
                    startActivity(intent)
                    finish()
                    true
                }
                R.id.nav_logout -> {
                    // Cierra sesión y regresa a la pantalla de Auth
                    val intent = Intent(this, com.example.bikelight.ui.auth.AuthActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                    startActivity(intent)
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun sendWifiCredentials(ssid: String, pass: String) {
        binding.progressBar.visibility = View.VISIBLE

        // Simulación de envío vía HTTP local (ej. 192.168.4.1)
        binding.root.postDelayed({
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Credenciales enviadas correctamente al dispositivo", Toast.LENGTH_LONG).show()
        }, 1500)
    }

    override fun onDestroy() {
        super.onDestroy()
        networkCallback?.let { connectivityManager?.unregisterNetworkCallback(it) }
        connectivityManager?.bindProcessToNetwork(null)
    }
}