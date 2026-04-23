package com.turbox.booster.ui.gaming

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.turbox.booster.databinding.FragmentGamingBinding
import com.turbox.booster.utils.GameUtils
import com.turbox.booster.utils.ShizukuHelper
import com.turbox.booster.utils.SystemUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GamingFragment : Fragment() {

    private var _binding: FragmentGamingBinding? = null
    private val binding get() = _binding!!
    private lateinit var gamesAdapter: GamesAdapter
    private var selectedProfile = GameUtils.PerformanceProfile.PERFORMANCE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGamingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupProfileChips()
        setupBoostButton()
        loadGames()
        updateGameStats()
    }

    private fun setupRecyclerView() {
        gamesAdapter = GamesAdapter { game ->
            launchGame(game)
        }
        binding.rvGames.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = gamesAdapter
        }
    }

    private fun setupProfileChips() {
        binding.chipBattery.setOnClickListener { selectedProfile = GameUtils.PerformanceProfile.BATTERY_SAVER }
        binding.chipBalanced.setOnClickListener { selectedProfile = GameUtils.PerformanceProfile.BALANCED }
        binding.chipPerformance.setOnClickListener { selectedProfile = GameUtils.PerformanceProfile.PERFORMANCE }
        binding.chipExtreme.setOnClickListener { selectedProfile = GameUtils.PerformanceProfile.EXTREME }

        // Seleccionar Performance por defecto
        binding.chipPerformance.isChecked = true

        val hasShizuku = ShizukuHelper.isShizukuAvailable() && ShizukuHelper.hasShizukuPermission()
        if (!hasShizuku) {
            binding.chipPerformance.isEnabled = false
            binding.chipExtreme.isEnabled = false
            binding.chipBattery.isEnabled = false
            binding.tvProfileHint.visibility = View.VISIBLE
        }
    }

    private fun setupBoostButton() {
        binding.btnBoostNow.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                binding.btnBoostNow.isEnabled = false
                binding.btnBoostNow.text = "Aplicando..."
                withContext(Dispatchers.IO) {
                    GameUtils.applyPerformanceProfile(requireContext(), selectedProfile)
                    SystemUtils.triggerGarbageCollection()
                }
                binding.btnBoostNow.isEnabled = true
                binding.btnBoostNow.text = "BOOST APLICADO ✓"
                Snackbar.make(binding.root, "¡Perfil ${selectedProfile.name} activado!", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadGames() {
        binding.progressGames.visibility = View.VISIBLE
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val games = GameUtils.getInstalledGames(requireContext())
            withContext(Dispatchers.Main) {
                if (_binding == null) return@withContext
                binding.progressGames.visibility = View.GONE
                if (games.isEmpty()) {
                    binding.tvNoGames.visibility = View.VISIBLE
                    binding.rvGames.visibility = View.GONE
                } else {
                    binding.tvNoGames.visibility = View.GONE
                    binding.rvGames.visibility = View.VISIBLE
                    gamesAdapter.submitList(games)
                }
            }
        }
    }

    private fun updateGameStats() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val ram = SystemUtils.getRamInfo(requireContext())
            val temp = SystemUtils.getCpuTemperature()
            withContext(Dispatchers.Main) {
                if (_binding == null) return@withContext
                binding.tvRamAvailable.text = "${ram.availableMb} MB libres"
                binding.tvTempCurrent.text = if (temp > 0) "${temp.toInt()}°C" else "--°C"
                binding.tvGameReadiness.text = when {
                    ram.availableMb > 1500 && (temp == 0f || temp < 45) -> "✓ Listo para jugar"
                    ram.availableMb > 800 -> "⚡ Optimiza para mejor rendimiento"
                    else -> "⚠️ RAM baja, optimiza primero"
                }
            }
        }
    }

    private fun launchGame(game: GameUtils.GameApp) {
        viewLifecycleOwner.lifecycleScope.launch {
            Snackbar.make(binding.root, "Lanzando ${game.name} con boost...", Snackbar.LENGTH_SHORT).show()
            withContext(Dispatchers.IO) {
                GameUtils.launchGameOptimized(requireContext(), game.packageName)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
