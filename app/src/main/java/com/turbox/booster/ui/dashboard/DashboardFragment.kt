package com.turbox.booster.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.turbox.booster.R
import com.turbox.booster.databinding.FragmentDashboardBinding
import com.turbox.booster.utils.SystemUtils
import kotlinx.coroutines.*
import java.util.LinkedList

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private var updateJob: Job? = null
    private val cpuHistory = LinkedList<Float>()
    private val ramHistory = LinkedList<Float>()
    private val MAX_HISTORY = 30

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCharts()
        startUpdating()
    }

    private fun setupCharts() {
        // CPU Chart
        binding.cpuChart.apply {
            description.isEnabled = false
            setTouchEnabled(false)
            legend.isEnabled = false
            axisRight.isEnabled = false
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
                axisMaximum = 100f
                textColor = context.getColor(R.color.colorOnSurface)
                gridColor = context.getColor(R.color.colorSurfaceVariant)
            }
            xAxis.apply {
                setDrawGridLines(false)
                setDrawLabels(false)
            }
            setBackgroundColor(context.getColor(android.R.color.transparent))
        }

        // RAM Chart
        binding.ramChart.apply {
            description.isEnabled = false
            setTouchEnabled(false)
            legend.isEnabled = false
            axisRight.isEnabled = false
            axisLeft.apply {
                setDrawGridLines(true)
                axisMinimum = 0f
                axisMaximum = 100f
                textColor = context.getColor(R.color.colorOnSurface)
                gridColor = context.getColor(R.color.colorSurfaceVariant)
            }
            xAxis.apply {
                setDrawGridLines(false)
                setDrawLabels(false)
            }
            setBackgroundColor(context.getColor(android.R.color.transparent))
        }
    }

    private fun startUpdating() {
        updateJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                updateStats()
                delay(1500)
            }
        }
    }

    private suspend fun updateStats() {
        withContext(Dispatchers.IO) {
            val cpuUsage = SystemUtils.getCpuUsage()
            val ramInfo = SystemUtils.getRamInfo(requireContext())
            val temp = SystemUtils.getCpuTemperature()
            val battery = SystemUtils.getBatteryInfo(requireContext())
            val storage = SystemUtils.getStorageInfo()
            val score = SystemUtils.calculateSystemScore(requireContext())
            val freqs = SystemUtils.getCpuFrequencies()
            val (swapTotal, swapFree) = SystemUtils.getSwapInfo()

            withContext(Dispatchers.Main) {
                if (_binding == null) return@withContext

                // Score principal
                binding.tvSystemScore.text = score.toString()
                binding.scoreProgress.progress = score
                binding.tvScoreLabel.text = when {
                    score >= 80 -> "Excelente"
                    score >= 60 -> "Bueno"
                    score >= 40 -> "Regular"
                    else -> "Necesita optimización"
                }
                binding.scoreProgress.setIndicatorColor(
                    requireContext().getColor(
                        when {
                            score >= 80 -> R.color.colorGreen
                            score >= 60 -> R.color.colorYellow
                            else -> R.color.colorRed
                        }
                    )
                )

                // CPU
                binding.tvCpuPercent.text = "${cpuUsage.toInt()}%"
                binding.cpuProgress.progress = cpuUsage.toInt()
                binding.tvCpuGovernor.text = "Gov: ${SystemUtils.getCpuGovernor()}"
                val maxFreqMhz = (freqs.maxOrNull() ?: 0L) / 1000
                binding.tvCpuFreq.text = if (maxFreqMhz > 0) "${maxFreqMhz} MHz" else "N/A"

                // RAM
                binding.tvRamUsed.text = "${ramInfo.usedMb} MB"
                binding.tvRamTotal.text = "/ ${ramInfo.totalMb} MB"
                binding.tvRamPercent.text = "${ramInfo.usagePercent.toInt()}%"
                binding.ramProgress.progress = ramInfo.usagePercent.toInt()
                if (swapTotal > 0) {
                    binding.tvSwap.text = "SWAP: ${swapTotal - swapFree}/${swapTotal} MB"
                    binding.tvSwap.visibility = View.VISIBLE
                }

                // Temperatura
                binding.tvTemp.text = if (temp > 0) "${temp.toInt()}°C" else "N/A"
                binding.tvTempStatus.text = when {
                    temp <= 0 -> ""
                    temp < 35 -> "Frío"
                    temp < 45 -> "Normal"
                    temp < 55 -> "Caliente"
                    else -> "¡Muy caliente!"
                }
                binding.tempProgress.progress = (temp / 80 * 100).toInt().coerceIn(0, 100)
                binding.tvTempStatus.setTextColor(
                    requireContext().getColor(
                        when {
                            temp <= 0 || temp < 35 -> R.color.colorGreen
                            temp < 45 -> R.color.colorYellow
                            else -> R.color.colorRed
                        }
                    )
                )

                // Batería
                binding.tvBatteryLevel.text = "${battery.level}%"
                binding.tvBatteryStatus.text = battery.status
                binding.tvBatteryVoltage.text = "${battery.voltage} V"
                binding.batteryProgress.progress = battery.level

                // Almacenamiento
                binding.tvStorageUsed.text = "${String.format("%.1f", storage.usedGb)} GB"
                binding.tvStorageTotal.text = "/ ${String.format("%.1f", storage.totalGb)} GB"
                binding.storageProgress.progress = storage.usagePercent.toInt()

                // Actualizar graficas
                updateChart(cpuUsage, ramInfo.usagePercent)
            }
        }
    }

    private fun updateChart(cpuUsage: Float, ramUsage: Float) {
        cpuHistory.add(cpuUsage)
        ramHistory.add(ramUsage)
        if (cpuHistory.size > MAX_HISTORY) cpuHistory.removeFirst()
        if (ramHistory.size > MAX_HISTORY) ramHistory.removeFirst()

        // CPU chart
        val cpuEntries = cpuHistory.mapIndexed { i, v -> Entry(i.toFloat(), v) }
        val cpuDataSet = LineDataSet(cpuEntries, "CPU").apply {
            color = requireContext().getColor(R.color.colorPrimary)
            setDrawCircles(false)
            lineWidth = 2f
            fillColor = requireContext().getColor(R.color.colorPrimaryLight)
            setDrawFilled(true)
            fillAlpha = 60
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        binding.cpuChart.data = LineData(cpuDataSet)
        binding.cpuChart.notifyDataSetChanged()
        binding.cpuChart.invalidate()

        // RAM chart
        val ramEntries = ramHistory.mapIndexed { i, v -> Entry(i.toFloat(), v) }
        val ramDataSet = LineDataSet(ramEntries, "RAM").apply {
            color = requireContext().getColor(R.color.colorSecondary)
            setDrawCircles(false)
            lineWidth = 2f
            fillColor = requireContext().getColor(R.color.colorSecondaryLight)
            setDrawFilled(true)
            fillAlpha = 60
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        binding.ramChart.data = LineData(ramDataSet)
        binding.ramChart.notifyDataSetChanged()
        binding.ramChart.invalidate()
    }

    override fun onPause() {
        super.onPause()
        updateJob?.cancel()
    }

    override fun onResume() {
        super.onResume()
        if (updateJob?.isActive != true) {
            startUpdating()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        updateJob?.cancel()
        _binding = null
    }
}
