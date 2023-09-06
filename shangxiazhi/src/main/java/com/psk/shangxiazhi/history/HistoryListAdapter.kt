package com.psk.shangxiazhi.history

import androidx.recyclerview.widget.DiffUtil
import com.like.recyclerview.adapter.BaseListAdapter
import com.like.recyclerview.viewholder.BindingViewHolder
import com.psk.shangxiazhi.R
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

    override fun onBindViewHolder(holder: BindingViewHolder<ItemHistoryBinding>, item: DateAndData?) {
        item ?: return
        holder.binding.tvDate.text = "${item.month.format2()}月${item.day.format2()}日"
        holder.binding.tvTime.text = "${item.hour.format2()}:${item.minute.format2()}:${item.second.format2()}"
    }

    override fun getItemViewType(position: Int, item: DateAndData?): Int {
        return R.layout.item_history
    }
}
