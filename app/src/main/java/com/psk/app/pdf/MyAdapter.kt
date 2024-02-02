package com.psk.app.pdf

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.psk.app.BR
import com.psk.app.R
import com.psk.app.databinding.ItemBinding

open class MyAdapter : ListAdapter<Item, BindingViewHolder<ItemBinding>>(
    object : DiffUtil.ItemCallback<Item>() {
        override fun areItemsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Item, newItem: Item): Boolean {
            return oldItem.name == newItem.name
        }
    }
) {

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder<ItemBinding> {
        return BindingViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item, parent, false))
    }

    final override fun onBindViewHolder(holder: BindingViewHolder<ItemBinding>, position: Int) {
        val item = try {
            getItem(holder.bindingAdapterPosition)
        } catch (e: Exception) {
            null
        }
        holder.binding.setVariable(BR.item, item)
    }

}
