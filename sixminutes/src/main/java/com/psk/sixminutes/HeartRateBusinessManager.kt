package com.psk.sixminutes

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.like.common.util.showToast
import com.psk.device.DeviceRepositoryManager
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.HeartRateRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class HeartRateBusinessManager {
    private val repository = DeviceRepositoryManager.createBleDeviceRepository<HeartRateRepository>(DeviceType.HeartRate)
    private var job: Job? = null
    private val isInitialized = AtomicBoolean(false)

    fun init(context: Context, name: String, address: String) {
        if (isInitialized.compareAndSet(false, true)) {
            repository.init(context, name, address)
        }
    }

    fun getSampleRate(): Int {
        checkInit()
        return repository.getSampleRate()
    }

    fun connect(context: Context, lifecycleScope: LifecycleCoroutineScope, onResult: (List<List<Float>>) -> Unit) {
        checkInit()
        lifecycleScope.launch {
            repository.connect(lifecycleScope, 0L, {
                context.showToast("心电仪连接成功")
                job = lifecycleScope.launch {
                    repository.fetch().filterNotNull().map {
                        it.coorYValues
                    }.buffer(Int.MAX_VALUE).collect {
                        // 取反，因为如果不处理，画出的波形图是反的
                        onResult(listOf(it.map { -it }))
                    }
                }
            }) {
                context.showToast("心电仪连接失败")
                job?.cancel()
                job = null
            }
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