package com.psk.shangxiazhi.controller

import androidx.recyclerview.widget.DiffUtil
import com.like.recyclerview.adapter.BaseListAdapter
import com.like.recyclerview.viewholder.BindingViewHolder
import com.psk.shangxiazhi.controller.databinding.ItemBleScanBinding

class ScanDeviceAdapter : BaseListAdapter<ItemBleScanBinding, BleScanInfo>(DIFF) {

    override fun onBindViewHolder(holder: BindingViewHolder<ItemBleScanBinding>, item: BleScanInfo?) {
        super.onBindViewHolder(holder, item)
        item ?: return
        val binding = holder.binding

    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<BleScanInfo>() {
            override fun areItemsTheSame(oldItem: BleScanInfo, newItem: BleScanInfo): Boolean {
                return oldItem.address == newItem.address
            }

            override fun areContentsTheSame(oldItem: BleScanInfo, newItem: BleScanInfo): Boolean {
                return oldItem.name == newItem.name
            }
        }
    }
}