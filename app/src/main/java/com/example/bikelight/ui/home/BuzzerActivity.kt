package com.example.bikelight.ui.home

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

// Importante para acceder a tus recursos (colores, layouts, ids)
import com.example.bikelight.R

class BuzzerActivity : AppCompatActivity() {

    private var toneGenerator: ToneGenerator? = null
    private var isSimulando: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buzzer_config)

        // Generador de tono para simular el zumbador
        toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)

        val spinnerFrecuencia = findViewById<Spinner>(R.id.spinnerFrecuencia)
        val spinnerDuracion = findViewById<Spinner>(R.id.spinnerDuracion)
        val btnActivar = findViewById<Button>(R.id.btnActivar)
        val btnDesactivar = findViewById<Button>(R.id.btnDesactivar)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Listas de selección
        val opcionesFrecuencia = arrayOf("Seleccione una opción", "Opción 1 (1000 Hz)", "Opción 2 (2400 Hz)")
        val opcionesDuracion = arrayOf("Seleccione una opción", "0.10s", "0.50s", "1.00s")

        val adapterFrec = ArrayAdapter(this, R.layout.item_spinner_buzzer, opcionesFrecuencia)
        val adapterDur = ArrayAdapter(this, R.layout.item_spinner_buzzer, opcionesDuracion)

        spinnerFrecuencia.adapter = adapterFrec
        spinnerDuracion.adapter = adapterDur

        // Botón Activar simulación
        btnActivar.setOnClickListener {
            val duracionTexto = spinnerDuracion.selectedItem.toString()
            val duracionMs = when {
                duracionTexto.contains("0.10s") -> 100
                duracionTexto.contains("0.50s") -> 500
                duracionTexto.contains("1.00s") -> 1000
                else -> 200
            }

            isSimulando = true
            ejecutarSimulacion(duracionMs)
            Toast.makeText(this, "Simulación Buzzer Activada ($duracionMs ms)", Toast.LENGTH_SHORT).show()
        }

        // Botón Desactivar
        btnDesactivar.setOnClickListener {
            isSimulando = false
            toneGenerator?.stopTone()
            Toast.makeText(this, "Buzzer Desactivado", Toast.LENGTH_SHORT).show()
        }

        // Acciones del BottomNavigationView nativo
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_perfil -> {
                    Toast.makeText(this, "Navegando a Perfil", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_home -> {
                    finish() // Regresa a Home
                    true
                }
                R.id.nav_logout -> {
                    Toast.makeText(this, "Cerrando sesión...", Toast.LENGTH_SHORT).show()
                    finishAffinity()
                    true
                }
                else -> false
            }
        }
    }

    private fun ejecutarSimulacion(duracionMs: Int) {
        // Tono audible
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, duracionMs)

        // Respuesta háptica (ya no marcará error en Android Studio al estar el permiso en el Manifest)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator.vibrate(
                VibrationEffect.createOneShot(duracionMs.toLong(), VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(duracionMs.toLong())
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        toneGenerator?.release()
        toneGenerator = null
    }
}