package com.psk.recovery.medicalorder.list

import androidx.recyclerview.widget.DiffUtil
import com.like.common.util.gone
import com.like.recyclerview.adapter.BaseListAdapter
import com.like.recyclerview.viewholder.BindingViewHolder
import com.psk.recovery.data.model.embedded.MedicalOrderAndMonitorDevice
import com.psk.recovery.databinding.ItemMedicalOrderBinding
import com.psk.recovery.medicalorder.execute.ExecuteMedicalOrderActivity
import com.psk.recovery.medicalorder.history.HistoryMedicalOrderActivity
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.text.SimpleDateFormat

class MedicalOrderListAdapter : BaseListAdapter<ItemMedicalOrderBinding, MedicalOrderAndMonitorDevice>(DIFF), KoinComponent {
    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<MedicalOrderAndMonitorDevice>() {
            override fun areItemsTheSame(oldItem: MedicalOrderAndMonitorDevice, newItem: MedicalOrderAndMonitorDevice): Boolean {
                return oldItem.medicalOrder == newItem.medicalOrder
            }

            override fun areContentsTheSame(oldItem: MedicalOrderAndMonitorDevice, newItem: MedicalOrderAndMonitorDevice): Boolean {
                return oldItem.monitorDevices == newItem.monitorDevices
            }

        }
    }

    @OptIn(KoinApiExtension::class)
    private val sdf: SimpleDateFormat by inject(named("yyyy-MM-dd"))

    override fun onBindViewHolder(holder: BindingViewHolder<ItemMedicalOrderBinding>, item: MedicalOrderAndMonitorDevice?) {
        item ?: return
        val binding = holder.binding
        binding.tvDate.text = sdf.format(item.medicalOrder.planExecuteTime * 1000)
        if (item.medicalOrder.planExecuteTime / 60 / 60 / 24 == System.currentTimeMillis() / 1000 / 60 / 60 / 24) {
            // 如果是当天
            when (item.medicalOrder.status) {
                0 -> {
                    binding.btnFun.text = "执行"
                    binding.btnFun.setOnClickListener {
                        ExecuteMedicalOrderActivity.start(item)
                    }
                }

                1 -> {
                    binding.btnFun.text = "继续执行"
                    binding.btnFun.setOnClickListener {
                        ExecuteMedicalOrderActivity.start(item)
                    }
                }

                2 -> {
                    binding.btnFun.text = "回放"
                    binding.btnFun.setOnClickListener {
                        HistoryMedicalOrderActivity.start(item)
                    }
                }

                else -> {
                    binding.btnFun.text = ""
                    binding.btnFun.setOnClickListener(null)
                    binding.btnFun.gone()
                }
            }
        } else {
            // 如果不是当天
            when (item.medicalOrder.status) {
                1, 2 -> {
                    binding.btnFun.text = "回放"
                    binding.btnFun.setOnClickListener {
                        HistoryMedicalOrderActivity.start(item)
                    }
                }

                else -> {
                    binding.btnFun.text = ""
                    binding.btnFun.setOnClickListener(null)
                    binding.btnFun.gone()
                }
            }
        }
    }
}
