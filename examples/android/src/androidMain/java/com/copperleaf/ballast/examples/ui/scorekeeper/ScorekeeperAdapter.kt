package com.copperleaf.ballast.examples.ui.scorekeeper

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.copperleaf.ballast.examples.R
import com.copperleaf.ballast.examples.databinding.ListItemScorekeeperBinding
import com.copperleaf.ballast.examples.ui.scorekeeper.models.Player

class ScorekeeperAdapter(
    private val items: List<Player>,
    private val postInput: (ScorekeeperContract.Inputs) -> Unit,
) : RecyclerView.Adapter<ScorekeeperAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ListItemScorekeeperBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindPost(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(
        private val binding: ListItemScorekeeperBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bindPost(item: Player) {
            binding.tvPlayerName.text = item.name
            binding.tvScore.text = item.scoreDisplay

            if (item.selected) {
                binding.tvPlayerName.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.purple_500)
                )
            } else {
                binding.tvPlayerName.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.black)
                )
            }

            if (item.tempScore != 0) {
                binding.tvScore.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.purple_500)
                )
            } else {
                binding.tvScore.setTextColor(
                    ContextCompat.getColor(binding.root.context, R.color.black)
                )
            }

            binding.root.setOnClickListener {
                postInput(ScorekeeperContract.Inputs.TogglePlayerSelection(item.name))
            }
            binding.btnRemove.setOnClickListener {
                postInput(ScorekeeperContract.Inputs.RemovePlayer(item.name))
            }
            binding.tvScore.setOnClickListener {
                postInput(ScorekeeperContract.Inputs.CommitTempScore(item.name))
            }
        }
    }
}
