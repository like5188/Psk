package com.psk.sixminutes.business.ble

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.like.common.util.showToast
import com.psk.device.DeviceRepositoryManager
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.repository.ble.BloodOxygenRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class BleBloodOxygenBusinessManager {
    private val repository = DeviceRepositoryManager.createBleDeviceRepository<BloodOxygenRepository>(DeviceType.BloodOxygen)
    private var job: Job? = null
    private val isInitialized = AtomicBoolean(false)
    private lateinit var lifecycleScope: LifecycleCoroutineScope
    private lateinit var context: Context

    fun init(activity: ComponentActivity, name: String, address: String) {
        if (isInitialized.compareAndSet(false, true)) {
            this.context = activity.applicationContext
            this.lifecycleScope = activity.lifecycleScope
            repository.init(context, name, address)
        }
    }

    fun connect(orderId: Long, onStatus: (String) -> Unit, onBloodOxygenResult: (Int) -> Unit) {
        checkInit()
        repository.connect(lifecycleScope, onConnected = {
            onStatus("已连接")
            context.showToast("血氧仪连接成功")
            val flow = repository.getFlow(lifecycleScope, orderId, 1000)
            job = lifecycleScope.launch {
                flow.distinctUntilChanged().conflate().collect { value ->
                    onBloodOxygenResult(value.value)
                }
            }
        }) {
            onStatus("已断开")
            context.showToast("血氧仪连接失败")
            job?.cancel()
            job = null
        }
    }

    fun disconnect() {
        job?.cancel()
        job = null
        // 避免未初始化时调用报错
        try {
            repository.close()
        } catch (e: Exception) {
        }
    }

    private fun checkInit() {
        check(isInitialized.get()) {
            "请先调用 init() 方法进行初始化"
        }
    }

}