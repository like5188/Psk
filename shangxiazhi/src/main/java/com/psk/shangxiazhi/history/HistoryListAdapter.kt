package com.psk.shangxiazhi.history

import androidx.recyclerview.widget.DiffUtil
import com.like.recyclerview.adapter.BaseListAdapter
import com.psk.shangxiazhi.databinding.ItemHistoryBinding

class HistoryListAdapter : BaseListAdapter<ItemHistoryBinding, DateAndData>(DIFF) {
    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<DateAndData>() {
            override fun areItemsTheSame(oldItem: DateAndData, newItem: DateAndData): Boolean {
                return oldItem.data == newItem.data
            }

            override fun areContentsTheSame(oldItem: DateAndData, newItem: DateAndData): Boolean {
                return oldItem.year == newItem.year &&
                        oldItem.month == newItem.month &&
                        oldItem.day == newItem.day &&
                        oldItem.hour == newItem.hour &&
                        oldItem.minute == newItem.minute &&
                        oldItem.second == newItem.second
            }

        }
    }

}
