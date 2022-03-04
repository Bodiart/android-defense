package com.bodiart.defense.features.main.adapter

import androidx.recyclerview.widget.RecyclerView
import com.bodiart.defense.R
import com.bodiart.defense.databinding.ListItemStatisticBinding
import com.bodiart.defense.util.extension.context

class StatisticViewHolder(val binding: ListItemStatisticBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: StatisticItem) {
        binding.run {
            textView.text = context.getString(
                R.string.main_attack_statistic,
                item.website,
                item.attacks.toString()
            )
        }
    }
}