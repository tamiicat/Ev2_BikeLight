package com.example.bikelight.ui.settings

import android.os.Bundle
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.bikelight.R
import com.example.bikelight.databinding.FragmentBorrarCacheBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class BorrarCacheFragment : Fragment() {
    private var _binding: FragmentBorrarCacheBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBorrarCacheBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.findViewById<TextView>(R.id.tvHeaderTitle)?.text = "Borrar Caché"

        calcularTamanoCache()

        binding.btnActualizarTamano.setOnClickListener {
            calcularTamanoCache()
        }

        binding.btnLimpiarCache.setOnClickListener {
            mostrarConfirmacionBorrado()
        }
    }

    private fun calcularTamanoCache() {
        lifecycleScope.launch(Dispatchers.IO) {
            val ctx = context ?: return@launch
            var totalBytes = 0L

            // caché interna principal
            totalBytes += obtenerTamanoCarpeta(ctx.cacheDir)

            // caché interna de código
            totalBytes += obtenerTamanoCarpeta(ctx.codeCacheDir)

            // cachés externas (si existen)
            ctx.externalCacheDirs?.filterNotNull()?.forEach { dir ->
                totalBytes += obtenerTamanoCarpeta(dir)
            }

            withContext(Dispatchers.Main) {
                if (_binding != null && isAdded) {
                    val formatoLegible = Formatter.formatFileSize(requireContext(), totalBytes)
                    binding.tvTamanoCache.text = formatoLegible
                }
            }
        }
    }

    private fun obtenerTamanoCarpeta(archivo: File?): Long {
        if (archivo == null || !archivo.exists()) return 0L
        var total: Long = 0
        val hijos = archivo.listFiles() ?: return 0L
        for (hijo in hijos) {
            total += if (hijo.isDirectory) {
                obtenerTamanoCarpeta(hijo)
            } else {
                hijo.length()
            }
        }
        return total
    }

    private fun mostrarConfirmacionBorrado() {
        AlertDialog.Builder(requireContext())
            .setTitle("Confirmar eliminación")
            .setMessage("¿Deseas vaciar la memoria caché de la aplicación? Solo se eliminarán archivos temporales.")
            .setPositiveButton("Vaciar") { _, _ ->
                ejecutarLimpieza()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun ejecutarLimpieza() {
        lifecycleScope.launch(Dispatchers.IO) {
            var exito = false
            try {
                val ctx = context ?: return@launch

                // vaciar sin eliminar las carpetas raíz del sistema
                ctx.cacheDir?.listFiles()?.forEach { it.deleteRecursively() }
                ctx.codeCacheDir?.listFiles()?.forEach { it.deleteRecursively() }

                ctx.externalCacheDirs?.filterNotNull()?.forEach { dir ->
                    dir.listFiles()?.forEach { it.deleteRecursively() }
                }

                exito = true
            } catch (e: Exception) {
                e.printStackTrace()
            }

            withContext(Dispatchers.Main) {
                if (isAdded) {
                    if (exito) {
                        Toast.makeText(requireContext(), "Caché eliminada con éxito", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "No se pudo limpiar parte de la caché", Toast.LENGTH_SHORT).show()
                    }
                    calcularTamanoCache()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}