package com.psk.shangxiazhi.history

import androidx.recyclerview.widget.DiffUtil
import com.like.recyclerview.adapter.BaseListAdapter
import com.like.recyclerview.viewholder.BindingViewHolder
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat

class HistoryListAdapter : BaseListAdapter<ItemHistoryBinding, Map.Entry<Long, Long>>(DIFF) {
    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Map.Entry<Long, Long>>() {
            override fun areItemsTheSame(oldItem: Map.Entry<Long, Long>, newItem: Map.Entry<Long, Long>): Boolean {
                return oldItem.key == newItem.key
            }

            override fun areContentsTheSame(oldItem: Map.Entry<Long, Long>, newItem: Map.Entry<Long, Long>): Boolean {
                return oldItem.value == newItem.value
            }

        }
    }

    private val sdf = SimpleDateFormat("MM月dd日")
    private val sdf1 = SimpleDateFormat("HH:mm:ss")

    override fun onBindViewHolder(holder: BindingViewHolder<ItemHistoryBinding>, item: Map.Entry<Long, Long>?) {
        item ?: return
        holder.binding.tvDate.text = sdf.format(item.value)
        holder.binding.tvTime.text = sdf1.format(item.value)
    }

    override fun getItemViewType(position: Int, item: Map.Entry<Long, Long>?): Int {
        return R.layout.item_history
    }
}
