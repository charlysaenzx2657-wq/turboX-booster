package com.turbox.booster.ui.advanced

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.turbox.booster.databinding.FragmentKernelBinding
import com.turbox.booster.utils.KernelUtils
import com.turbox.booster.utils.ShizukuHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class KernelFragment : Fragment() {

    private var _binding: FragmentKernelBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentKernelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadKernelInfo()
        setupButtons()
    }

    private fun loadKernelInfo() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val version = KernelUtils.getKernelVersion()
            val uptime = KernelUtils.getFormattedUptime()
            val (l1, l5, l15) = KernelUtils.getLoadAverage()
            val ioSched = KernelUtils.getIoScheduler()
            val tcp = KernelUtils.getTcpCongestion()
            val swappy = KernelUtils.getVmSwappiness()
            val memPressure = KernelUtils.getMemoryPressure()

            withContext(Dispatchers.Main) {
                if (_binding == null) return@withContext
                binding.tvKernelVersion.text = "Versión: $version"
                binding.tvUptime.text = uptime
                binding.tvLoadAvg.text = "${"%.2f".format(l1)} / ${"%.2f".format(l5)} / ${"%.2f".format(l15)}"
                binding.tvIoScheduler.text = ioSched
                binding.tvTcpCongestion.text = tcp
                binding.tvSwappiness.text = if (swappy >= 0) "$swappy" else "N/A"
                binding.tvMemPressure.text = memPressure
            }
        }
    }

    private fun setupButtons() {
        val hasShizuku = ShizukuHelper.isShizukuAvailable() && ShizukuHelper.hasShizukuPermission()

        binding.btnKernelGaming.setOnClickListener {
            if (!hasShizuku) { snack("Requiere Shizuku"); return@setOnClickListener }
            viewLifecycleOwner.lifecycleScope.launch {
                binding.btnKernelGaming.isEnabled = false
                withContext(Dispatchers.IO) { KernelUtils.applyLowLatencyKernelTweaks(); delay(800) }
                binding.btnKernelGaming.isEnabled = true
                snack("Tweaks VM de gaming aplicados")
                loadKernelInfo()
            }
        }

        binding.btnKernelNetwork.setOnClickListener {
            if (!hasShizuku) { snack("Requiere Shizuku"); return@setOnClickListener }
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) { KernelUtils.applyNetworkKernelTweaks(); delay(500) }
                snack("Tweaks TCP/IP aplicados")
                loadKernelInfo()
            }
        }

        binding.btnIoDeadline.setOnClickListener {
            if (!hasShizuku) { snack("Requiere Shizuku"); return@setOnClickListener }
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) { KernelUtils.setIoScheduler("deadline"); delay(500) }
                snack("I/O scheduler → deadline")
                loadKernelInfo()
            }
        }

        binding.btnIoCfq.setOnClickListener {
            if (!hasShizuku) { snack("Requiere Shizuku"); return@setOnClickListener }
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) { KernelUtils.setIoScheduler("cfq"); delay(500) }
                snack("I/O scheduler → cfq")
                loadKernelInfo()
            }
        }

        binding.btnReadahead.setOnClickListener {
            if (!hasShizuku) { snack("Requiere Shizuku"); return@setOnClickListener }
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) { KernelUtils.setReadAheadKb(2048); delay(400) }
                snack("Read-ahead aumentado a 2MB")
            }
        }
    }

    private fun snack(msg: String) = Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
