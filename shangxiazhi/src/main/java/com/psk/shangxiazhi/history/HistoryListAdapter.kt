package com.psk.shangxiazhi.history

import androidx.recyclerview.widget.DiffUtil
import com.like.recyclerview.adapter.BaseListAdapter
import com.like.recyclerview.viewholder.BindingViewHolder
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat

class HistoryListAdapter : BaseListAdapter<ItemHistoryBinding, DateAndData>(DIFF) {
    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<DateAndData>() {
            override fun areItemsTheSame(oldItem: DateAndData, newItem: DateAndData): Boolean {
                return oldItem.data == newItem.data
            }

            override fun areContentsTheSame(oldItem: DateAndData, newItem: DateAndData): Boolean {
                return oldItem.time == newItem.time
            }

        }
    }

    private val sdf = SimpleDateFormat("MM月dd日")
    private val sdf1 = SimpleDateFormat("HH:mm:ss")

    override fun onBindViewHolder(holder: BindingViewHolder<ItemHistoryBinding>, item: DateAndData?) {
        item ?: return
        holder.binding.tvDate.text = sdf.format(item.time)
        holder.binding.tvTime.text = sdf1.format(item.time)
    }

    override fun getItemViewType(position: Int, item: DateAndData?): Int {
        return R.layout.item_history
    }
}
