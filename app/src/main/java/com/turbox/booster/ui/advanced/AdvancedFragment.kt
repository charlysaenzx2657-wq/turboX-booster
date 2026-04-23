package com.turbox.booster.ui.advanced

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.turbox.booster.databinding.FragmentAdvancedBinding
import com.turbox.booster.utils.ShizukuHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdvancedFragment : Fragment() {

    private var _binding: FragmentAdvancedBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdvancedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateShizukuStatus()
        setupShizukuButton()
        setupAnimationSliders()
        setupSystemTweaks()
        setupNetworkSection()
        setupDangerZone()
        setupFreeSection()
    }

    // ─── STATUS SHIZUKU ──────────────────────────────────────────────────────

    private fun updateShizukuStatus() {
        val available = ShizukuHelper.isShizukuAvailable()
        val hasPermission = ShizukuHelper.hasShizukuPermission()

        when {
            !available -> {
                binding.chipShizukuStatus.text = "Shizuku no instalado"
                binding.chipShizukuStatus.setChipBackgroundColorResource(com.turbox.booster.R.color.colorRed)
                binding.cardShizukuInfo.visibility = View.VISIBLE
                setAdvancedEnabled(false)
            }
            !hasPermission -> {
                binding.chipShizukuStatus.text = "Sin permiso"
                binding.chipShizukuStatus.setChipBackgroundColorResource(com.turbox.booster.R.color.colorYellow)
                setAdvancedEnabled(false)
            }
            else -> {
                binding.chipShizukuStatus.text = "✓ Shizuku activo"
                binding.chipShizukuStatus.setChipBackgroundColorResource(com.turbox.booster.R.color.colorGreen)
                binding.cardShizukuInfo.visibility = View.GONE
                setAdvancedEnabled(true)
                ShizukuHelper.bindService()
            }
        }
    }

    private fun setAdvancedEnabled(enabled: Boolean) {
        binding.cardAnimations.alpha = if (enabled) 1f else 0.4f
        binding.cardSystemTweaks.alpha = if (enabled) 1f else 0.4f
        binding.cardNetwork.alpha = if (enabled) 1f else 0.4f
        binding.cardDangerZone.alpha = if (enabled) 1f else 0.4f

        // Deshabilitar controles
        val views = listOf(
            binding.seekWindowAnim, binding.seekTransitionAnim, binding.seekAnimatorDuration,
            binding.switchGpuRendering, binding.switchForceHwUi, binding.switchDisableBatterySaver,
            binding.switchWifiOptimize, binding.btnDnsCloudflare, binding.btnDnsGoogle,
            binding.btnDnsAdblock, binding.btnKillAll, binding.btnTrimCaches,
            binding.btnClearThermal, binding.btnPerfMode, binding.btnResetAnimations,
            binding.btnDisableAnimations, binding.btnBackgroundLimit0,
            binding.btnBackgroundLimit3, binding.btnBackgroundLimit6
        )
        views.forEach { it.isEnabled = enabled }
    }

    private fun setupShizukuButton() {
        binding.btnRequestShizuku.setOnClickListener {
            if (!ShizukuHelper.isShizukuAvailable()) {
                // Ir a Play Store
                try {
                    startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=moe.shizuku.privileged.api")))
                } catch (e: Exception) {
                    startActivity(Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=moe.shizuku.privileged.api")))
                }
            } else {
                ShizukuHelper.requestPermission()
                ShizukuHelper.addPermissionListener { granted ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        updateShizukuStatus()
                        if (granted) snack("✓ Permiso Shizuku concedido")
                    }
                }
            }
        }
    }

    // ─── ANIMACIONES ─────────────────────────────────────────────────────────

    private fun setupAnimationSliders() {
        // Window animation scale
        binding.seekWindowAnim.apply {
            max = 20 // 0.0x a 2.0x (paso de 0.1)
            progress = 10
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                    val scale = p / 10f
                    binding.tvWindowAnimValue.text = "${scale}x"
                    if (fromUser) ShizukuHelper.executeCommand(
                        "settings put global window_animation_scale $scale"
                    )
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }

        // Transition animation scale
        binding.seekTransitionAnim.apply {
            max = 20
            progress = 10
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                    val scale = p / 10f
                    binding.tvTransitionAnimValue.text = "${scale}x"
                    if (fromUser) ShizukuHelper.executeCommand(
                        "settings put global transition_animation_scale $scale"
                    )
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }

        // Animator duration scale
        binding.seekAnimatorDuration.apply {
            max = 20
            progress = 10
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                    val scale = p / 10f
                    binding.tvAnimatorDurationValue.text = "${scale}x"
                    if (fromUser) ShizukuHelper.executeCommand(
                        "settings put global animator_duration_scale $scale"
                    )
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }

        binding.btnDisableAnimations.setOnClickListener {
            ShizukuHelper.disableAnimations()
            binding.seekWindowAnim.progress = 0
            binding.seekTransitionAnim.progress = 0
            binding.seekAnimatorDuration.progress = 0
            snack("Animaciones desactivadas")
        }

        binding.btnResetAnimations.setOnClickListener {
            ShizukuHelper.setAnimations(1.0f)
            binding.seekWindowAnim.progress = 10
            binding.seekTransitionAnim.progress = 10
            binding.seekAnimatorDuration.progress = 10
            snack("Animaciones restauradas a 1.0x")
        }
    }

    // ─── SYSTEM TWEAKS ───────────────────────────────────────────────────────

    private fun setupSystemTweaks() {
        binding.switchGpuRendering.setOnCheckedChangeListener { _, checked ->
            ShizukuHelper.forceGpuRendering(checked)
            snack(if (checked) "GPU rendering activado" else "GPU rendering desactivado")
        }

        binding.switchForceHwUi.setOnCheckedChangeListener { _, checked ->
            ShizukuHelper.executeCommand(
                "settings put global force_hw_ui ${if (checked) "true" else "false"}"
            )
            snack(if (checked) "HW UI aceleración activada" else "HW UI aceleración desactivada")
        }

        binding.switchDisableBatterySaver.setOnCheckedChangeListener { _, checked ->
            if (checked) {
                ShizukuHelper.disableBatterySaver()
                snack("Ahorro de batería desactivado")
            } else {
                ShizukuHelper.executeCommand("settings put global low_power 1")
                snack("Ahorro de batería activado")
            }
        }

        // Background process limits
        binding.btnBackgroundLimit0.setOnClickListener {
            ShizukuHelper.setBackgroundProcessLimit(0) // Estándar
            snack("Límite estándar de procesos")
        }
        binding.btnBackgroundLimit3.setOnClickListener {
            ShizukuHelper.setBackgroundProcessLimit(3) // Máx 3
            snack("Máximo 3 procesos en background")
        }
        binding.btnBackgroundLimit6.setOnClickListener {
            ShizukuHelper.setBackgroundProcessLimit(0) // Sin límite
            snack("Sin límite de procesos background")
        }

        // Performance mode
        binding.btnPerfMode.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                binding.btnPerfMode.isEnabled = false
                withContext(Dispatchers.IO) {
                    ShizukuHelper.setPerformanceMode()
                    ShizukuHelper.optimizeGpu()
                }
                binding.btnPerfMode.isEnabled = true
                snack("Modo performance activado")
            }
        }

        // Kill all background
        binding.btnKillAll.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Cerrar apps en background")
                .setMessage("¿Cerrar todas las apps en segundo plano? Esto liberará RAM pero también cerrará apps que necesites.")
                .setPositiveButton("Cerrar todo") { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        withContext(Dispatchers.IO) { ShizukuHelper.killAllBackgroundProcesses() }
                        snack("Apps en background cerradas")
                    }
                }
                .setNegativeButton("Cancelar", null)
                .show()
        }

        // Trim caches
        binding.btnTrimCaches.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                binding.btnTrimCaches.isEnabled = false
                binding.btnTrimCaches.text = "Limpiando..."
                withContext(Dispatchers.IO) {
                    ShizukuHelper.trimAllCaches()
                    delay(1500)
                }
                binding.btnTrimCaches.isEnabled = true
                binding.btnTrimCaches.text = "Limpiar caché"
                snack("Caché del sistema limpiada")
            }
        }

        // Clear thermal
        binding.btnClearThermal.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) { ShizukuHelper.clearThermalThrottle() }
                snack("Throttling térmico liberado")
            }
        }
    }

    // ─── NETWORK ─────────────────────────────────────────────────────────────

    private fun setupNetworkSection() {
        binding.switchWifiOptimize.setOnCheckedChangeListener { _, checked ->
            ShizukuHelper.setWifiAggressiveScan(!checked)
            snack(if (checked) "WiFi optimizado (menos escaneo)" else "WiFi normal")
        }

        binding.btnDnsCloudflare.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) { ShizukuHelper.setDns("1.1.1.1", "1.0.0.1") }
                snack("DNS → Cloudflare (1.1.1.1)")
            }
        }

        binding.btnDnsGoogle.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) { ShizukuHelper.setDns("8.8.8.8", "8.8.4.4") }
                snack("DNS → Google (8.8.8.8)")
            }
        }

        binding.btnDnsAdblock.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                // AdGuard DNS con bloqueo de anuncios
                withContext(Dispatchers.IO) { ShizukuHelper.setDns("94.140.14.14", "94.140.15.15") }
                snack("DNS → AdGuard (bloqueo de ads)")
            }
        }

        binding.btnTcpOptimize.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) { ShizukuHelper.setTcpOptimizations() }
                snack("TCP/IP optimizado")
            }
        }
    }

    // ─── DANGER ZONE ─────────────────────────────────────────────────────────

    private fun setupDangerZone() {
        binding.btnDensityChange.setOnClickListener {
            showDensityDialog()
        }

        binding.btnDensityReset.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) { ShizukuHelper.resetScreenDensity() }
                snack("Densidad de pantalla restaurada")
            }
        }

        binding.btnEnableDevOptions.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) { ShizukuHelper.enableDeveloperOptions() }
                snack("Opciones de desarrollador habilitadas")
            }
        }
    }

    private fun showDensityDialog() {
        val options = arrayOf("360 dpi (compacto)", "420 dpi (normal)", "480 dpi (grande)", "540 dpi (muy grande)")
        val values = intArrayOf(360, 420, 480, 540)
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cambiar densidad de pantalla")
            .setMessage("⚠️ Cambia el tamaño de los elementos en pantalla")
            .setItems(options) { _, which ->
                viewLifecycleOwner.lifecycleScope.launch {
                    withContext(Dispatchers.IO) { ShizukuHelper.setScreenDensity(values[which]) }
                    snack("Densidad cambiada a ${values[which]} dpi")
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ─── FUNCIONES SIN ROOT ──────────────────────────────────────────────────

    private fun setupFreeSection() {
        // Estas funciones NO necesitan Shizuku

        binding.btnBatteryOptimization.setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            } catch (e: Exception) {
                startActivity(Intent(Settings.ACTION_SETTINGS))
            }
        }

        binding.btnDeveloperOptions.setOnClickListener {
            startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
        }

        binding.btnAppSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_MANAGE_ALL_APPLICATIONS_SETTINGS))
        }

        binding.btnWifiSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
        }

        binding.btnGcNow.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    Runtime.getRuntime().gc()
                    System.gc()
                    System.runFinalization()
                    delay(500)
                }
                snack("Garbage Collection ejecutado")
            }
        }

        binding.btnTerminal.setOnClickListener {
            showTerminalDialog()
        }
    }

    private fun showTerminalDialog() {
        if (!ShizukuHelper.isShizukuAvailable() || !ShizukuHelper.hasShizukuPermission()) {
            snack("Necesitas Shizuku para usar el terminal")
            return
        }

        val editText = com.google.android.material.textfield.TextInputEditText(requireContext())
        editText.hint = "Escribe un comando shell..."

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Terminal (Shizuku)")
            .setView(editText)
            .setPositiveButton("Ejecutar") { _, _ ->
                val cmd = editText.text?.toString()?.trim() ?: return@setPositiveButton
                if (cmd.isNotEmpty()) {
                    viewLifecycleOwner.lifecycleScope.launch {
                        val result = withContext(Dispatchers.IO) { ShizukuHelper.executeCommand(cmd) }
                        showResultDialog(cmd, result)
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showResultDialog(command: String, result: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Resultado: $command")
            .setMessage(result.ifEmpty { "(sin salida)" })
            .setPositiveButton("OK", null)
            .show()
    }

    private fun snack(msg: String) {
        Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        updateShizukuStatus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
