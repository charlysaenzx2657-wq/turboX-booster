package com.turbox.booster.ui.gaming

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.turbox.booster.databinding.ItemGameBinding
import com.turbox.booster.utils.GameUtils

class GamesAdapter(
    private val onLaunch: (GameUtils.GameApp) -> Unit
) : ListAdapter<GameUtils.GameApp, GamesAdapter.GameViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val binding = ItemGameBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return GameViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class GameViewHolder(
        private val binding: ItemGameBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(game: GameUtils.GameApp) {
            binding.tvGameName.text = game.name
            binding.tvPackageName.text = game.packageName
            if (game.icon != null) {
                binding.ivGameIcon.setImageDrawable(game.icon)
            } else {
                binding.ivGameIcon.setImageResource(android.R.drawable.sym_def_app_icon)
            }
            binding.btnLaunch.setOnClickListener {
                onLaunch(game)
            }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GameUtils.GameApp>() {
            override fun areItemsTheSame(a: GameUtils.GameApp, b: GameUtils.GameApp) =
                a.packageName == b.packageName
            override fun areContentsTheSame(a: GameUtils.GameApp, b: GameUtils.GameApp) =
                a == b
        }
    }
}
