package com.example.bikelight.ui.home

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bikelight.R
import com.example.bikelight.databinding.FragmentBuzzerConfigBinding

class BuzzerFragment : Fragment() {
    private var _binding: FragmentBuzzerConfigBinding? = null
    private val binding get() = _binding!!
    private var toneGenerator: ToneGenerator? = null
    private var isSimulando: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBuzzerConfigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // generador de tono
        toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)

        // frecuencias buzzer
        val opcionesFrecuencia = arrayOf(
            "Seleccione una opción",
            "Tono Grave (1000 Hz)",
            "Tono Medio (1500 Hz)",
            "Estándar (2000 Hz)",
            "Alerta de Tráfico (2500 Hz)",
            "Tono Agudo (4000 Hz)"
        )

        // duración buzzer
        val opcionesDuracion = arrayOf(
            "Seleccione una opción",
            "Pulso Ultracorto (0.05s)",
            "Pitido Estándar(0.10s)",
            "Pulso Medio (0.25s)",
            "Advertencia (0.50)",
            "Tono Continuo (1.00)"
        )

        val adapterFrec = ArrayAdapter(requireContext(), R.drawable.item_spinner_buzzer, opcionesFrecuencia)
        val adapterDur = ArrayAdapter(requireContext(), R.drawable.item_spinner_buzzer, opcionesDuracion)

        binding.spinnerFrecuencia.adapter = adapterFrec
        binding.spinnerDuracion.adapter = adapterDur

        // Botón Activar
        binding.btnActivar.setOnClickListener {
            val duracionTexto = binding.spinnerDuracion.selectedItem.toString()
            val duracionMs = when {
                duracionTexto.contains("0.10s") -> 100
                duracionTexto.contains("0.50s") -> 500
                duracionTexto.contains("1.00s") -> 1000
                else -> 200
            }

            isSimulando = true
            ejecutarSimulacion(duracionMs)
            Toast.makeText(requireContext(), "Simulación Buzzer Activada ($duracionMs ms)", Toast.LENGTH_SHORT).show()
        }

        // Botón Desactivar
        binding.btnDesactivar.setOnClickListener {
            isSimulando = false
            toneGenerator?.stopTone()
            Toast.makeText(requireContext(), "Buzzer Desactivado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun ejecutarSimulacion(duracionMs: Int) {
        // Tono audible
        toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, duracionMs)

        // Vibración
        val context = context ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator.vibrate(
                VibrationEffect.createOneShot(duracionMs.toLong(), VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(duracionMs.toLong())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        toneGenerator?.release()
        toneGenerator = null
        _binding = null
    }
}