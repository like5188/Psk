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
        val month = if (item.month!! < 10) {
            "0${item.month}"
        } else {
            item.month.toString()
        }
        val day = if (item.day!! < 10) {
            "0${item.day}"
        } else {
            item.day.toString()
        }
        holder.binding.tvDate.text = "${month}月${day}日"

        val hour = if (item.hour!! < 10) {
            "0${item.hour}"
        } else {
            item.hour.toString()
        }
        val minute = if (item.minute!! < 10) {
            "0${item.minute}"
        } else {
            item.minute.toString()
        }
        val second = if (item.second!! < 10) {
            "0${item.second}"
        } else {
            item.second.toString()
        }
        holder.binding.tvTime.text = "${hour}:${minute}:${second}"
    }

    override fun getItemViewType(position: Int, item: DateAndData?): Int {
        return R.layout.item_history
    }
}
