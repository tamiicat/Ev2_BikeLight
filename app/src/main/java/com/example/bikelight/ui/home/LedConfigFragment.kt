package com.example.bikelight.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bikelight.R
import com.example.bikelight.databinding.FragmentLedConfigBinding

class LedConfigFragment : Fragment() {

    private var _binding: FragmentLedConfigBinding? = null
    private val binding get() = _binding!!

    // patrones LED
    private val opcionesColores = listOf(
        "Seleccione una opción",
        "Clásico (Rojo - Amarillo - Verde)",
        "Neón (Cian - Megenta - Amarillo)",
        "Emergencia (Azul - Blanco - Rojo)",
        "Flúor (Turquesa - Lima - Blanco)",
        "Arcoíris (Azul - Violeta - Rojo)",
        "Eco (Verde - Lima - Amarillo)",
        "Fuego (Rojo - Naranja - Amarillo)"
    )

    // efectos para las luces
    private val opcionesEfectos = listOf(
        "Seleccione una opción",
        "Fijo / Continuo",
        "Parpadeo lento",
        "Parpadeo rápido",
        "Hacia la izquierda",
        "Hacia la derecha",
        "División en el centro"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLedConfigBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configurarSpinnerColores()
        configurarSpinnerEfectos()
        configurarSeekBar()
        configurarBotonesManuales()
    }

    private fun configurarSpinnerColores() {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_spinner_buzzer,
            opcionesColores
        )
        binding.spinnerLedColors.adapter = adapter

        binding.spinnerLedColors.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    val colorSeleccionado = opcionesColores[position]
                    Toast.makeText(requireContext(), "Patrón: $colorSeleccionado", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun configurarSpinnerEfectos() {
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.item_spinner_buzzer,
            opcionesEfectos
        )
        binding.spinnerEffectColors.adapter = adapter

        binding.spinnerEffectColors.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) {
                    val efectoSeleccionado = opcionesEfectos[position]
                    Toast.makeText(requireContext(), "Efecto: $efectoSeleccionado", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun configurarSeekBar() {
        binding.seekBarBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Lógica de regulación en tiempo real (Bluetooth / MQTT)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBar?.let {
                    Toast.makeText(requireContext(), "Brillo: ${it.progress}%", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun configurarBotonesManuales() {
        binding.btnActivarLed.setOnClickListener {
            Toast.makeText(requireContext(), "LEDs encendidos manualmente", Toast.LENGTH_SHORT).show()
        }

        binding.btnDesactivarLed.setOnClickListener {
            Toast.makeText(requireContext(), "LEDs apagados", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = LedConfigFragment()
    }
}