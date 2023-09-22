package com.psk.shangxiazhi.history

import androidx.recyclerview.widget.DiffUtil
import com.like.recyclerview.adapter.BaseListAdapter
import com.like.recyclerview.viewholder.BindingViewHolder
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.ItemHistoryBinding
import java.text.DecimalFormat

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

    private val decimalFormat = DecimalFormat("00")

    override fun onBindViewHolder(holder: BindingViewHolder<ItemHistoryBinding>, item: DateAndData?) {
        item ?: return
        holder.binding.tvDate.text = "${decimalFormat.format(item.month)}月${decimalFormat.format(item.day)}日"
        holder.binding.tvTime.text =
            "${decimalFormat.format(item.hour)}:${decimalFormat.format(item.minute)}:${decimalFormat.format(item.second)}"
    }

    override fun getItemViewType(position: Int, item: DateAndData?): Int {
        return R.layout.item_history
    }
}
