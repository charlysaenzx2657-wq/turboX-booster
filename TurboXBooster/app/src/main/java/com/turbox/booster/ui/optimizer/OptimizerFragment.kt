package com.turbox.booster.ui.optimizer

import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.turbox.booster.databinding.FragmentOptimizerBinding
import com.turbox.booster.utils.ShizukuHelper
import com.turbox.booster.utils.SystemUtils
import kotlinx.coroutines.*

class OptimizerFragment : Fragment() {

    private var _binding: FragmentOptimizerBinding? = null
    private val binding get() = _binding!!
    private var isOptimizing = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOptimizerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateCurrentStats()
        setupOptimizeButton()
        setupCheckBoxes()
    }

    private fun updateCurrentStats() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val ram = SystemUtils.getRamInfo(requireContext())
            val score = SystemUtils.calculateSystemScore(requireContext())
            withContext(Dispatchers.Main) {
                if (_binding == null) return@withContext
                binding.tvRamBefore.text = "${ram.usedMb} MB usados"
                binding.tvScoreBefore.text = "Score actual: $score/100"
            }
        }
    }

    private fun setupCheckBoxes() {
        // Mostrar/ocultar opciones de Shizuku según disponibilidad
        val hasShizuku = ShizukuHelper.isShizukuAvailable() && ShizukuHelper.hasShizukuPermission()
        binding.cbKillApps.isEnabled = hasShizuku
        binding.cbDisableAnimations.isEnabled = hasShizuku
        binding.cbGpuAcceleration.isEnabled = hasShizuku
        binding.cbTrimCaches.isEnabled = hasShizuku
        binding.cbThermal.isEnabled = hasShizuku

        if (!hasShizuku) {
            binding.tvShizukuHint.visibility = View.VISIBLE
        }
    }

    private fun setupOptimizeButton() {
        binding.btnOptimize.setOnClickListener {
            if (!isOptimizing) startOptimization()
        }
    }

    private fun startOptimization() {
        isOptimizing = true
        binding.btnOptimize.isEnabled = false
        binding.optimizeProgress.visibility = View.VISIBLE
        binding.tvOptimizeStatus.visibility = View.VISIBLE

        val tasks = buildTaskList()

        viewLifecycleOwner.lifecycleScope.launch {
            var completed = 0
            val total = tasks.size

            for (task in tasks) {
                withContext(Dispatchers.Main) {
                    binding.tvOptimizeStatus.text = task.name
                    binding.optimizeProgress.progress = (completed.toFloat() / total * 100).toInt()
                }
                withContext(Dispatchers.IO) {
                    task.action()
                    delay(300)
                }
                completed++
            }

            // Obtener stats finales
            withContext(Dispatchers.IO) {
                val ramAfter = SystemUtils.getRamInfo(requireContext())
                val scoreAfter = SystemUtils.calculateSystemScore(requireContext())
                withContext(Dispatchers.Main) {
                    if (_binding == null) return@withContext
                    showResults(ramAfter.availableMb, scoreAfter)
                }
            }
        }
    }

    private fun buildTaskList(): List<OptimizationTask> {
        val tasks = mutableListOf<OptimizationTask>()

        // Siempre disponibles
        tasks.add(OptimizationTask("Liberando memoria RAM...") {
            SystemUtils.triggerGarbageCollection()
        })
        tasks.add(OptimizationTask("Optimizando procesos internos...") {
            Runtime.getRuntime().gc()
            System.gc()
            delay(200)
        })

        // Con Shizuku
        if (ShizukuHelper.isShizukuAvailable() && ShizukuHelper.hasShizukuPermission()) {
            if (binding.cbKillApps.isChecked) {
                tasks.add(OptimizationTask("Cerrando apps en segundo plano...") {
                    ShizukuHelper.killAllBackgroundProcesses()
                })
            }
            if (binding.cbDisableAnimations.isChecked) {
                tasks.add(OptimizationTask("Optimizando animaciones...") {
                    ShizukuHelper.disableAnimations()
                })
            }
            if (binding.cbGpuAcceleration.isChecked) {
                tasks.add(OptimizationTask("Activando aceleración GPU...") {
                    ShizukuHelper.optimizeGpu()
                })
            }
            if (binding.cbTrimCaches.isChecked) {
                tasks.add(OptimizationTask("Limpiando caché del sistema...") {
                    ShizukuHelper.trimAllCaches()
                })
            }
            if (binding.cbThermal.isChecked) {
                tasks.add(OptimizationTask("Liberando throttling térmico...") {
                    ShizukuHelper.clearThermalThrottle()
                })
            }
        }

        tasks.add(OptimizationTask("Finalizando optimización...") {
            delay(500)
        })

        return tasks
    }

    private fun showResults(ramFreedMb: Long, newScore: Int) {
        binding.optimizeProgress.progress = 100
        binding.tvOptimizeStatus.text = "¡Optimización completada!"
        binding.cardResults.visibility = View.VISIBLE

        binding.tvRamAfter.text = "$ramFreedMb MB libres"
        binding.tvScoreAfter.text = "Score nuevo: $newScore/100"

        // Animación del score
        val anim = ValueAnimator.ofInt(0, newScore)
        anim.duration = 1000
        anim.interpolator = DecelerateInterpolator()
        anim.addUpdateListener {
            binding.progressResult.progress = it.animatedValue as Int
        }
        anim.start()

        isOptimizing = false
        binding.btnOptimize.isEnabled = true
        binding.btnOptimize.text = "OPTIMIZAR DE NUEVO"
    }

    data class OptimizationTask(
        val name: String,
        val action: suspend () -> Unit
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
