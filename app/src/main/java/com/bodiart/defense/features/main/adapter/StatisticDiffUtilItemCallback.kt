package com.bodiart.defense.features.main.adapter

import androidx.recyclerview.widget.DiffUtil

class StatisticDiffUtilItemCallback : DiffUtil.ItemCallback<StatisticItem>() {
    override fun areItemsTheSame(oldItem: StatisticItem, newItem: StatisticItem): Boolean {
        return oldItem.website == newItem.website
    }

    override fun areContentsTheSame(oldItem: StatisticItem, newItem: StatisticItem): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: StatisticItem, newItem: StatisticItem): Any? {
        return if (oldItem != newItem) {
            newItem
        } else {
            null
        }
    }
}