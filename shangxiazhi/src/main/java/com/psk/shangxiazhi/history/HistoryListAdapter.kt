package com.psk.shangxiazhi.history

import androidx.recyclerview.widget.DiffUtil
import com.like.recyclerview.adapter.BaseListAdapter
import com.like.recyclerview.viewholder.BindingViewHolder
import com.psk.device.data.model.Order
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.ItemHistoryBinding
import java.text.SimpleDateFormat

class HistoryListAdapter : BaseListAdapter<ItemHistoryBinding, Order>(DIFF) {
    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<Order>() {
            override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
                return oldItem.createTime == newItem.createTime
            }

        }
    }

    private val sdf = SimpleDateFormat("MM月dd日")
    private val sdf1 = SimpleDateFormat("HH:mm:ss")

    override fun onBindViewHolder(holder: BindingViewHolder<ItemHistoryBinding>, item: Order?) {
        item ?: return
        holder.binding.tvDate.text = sdf.format(item.createTime)
        holder.binding.tvTime.text = sdf1.format(item.createTime)
    }

    override fun getItemViewType(position: Int, item: Order?): Int {
        return R.layout.item_history
    }
}
