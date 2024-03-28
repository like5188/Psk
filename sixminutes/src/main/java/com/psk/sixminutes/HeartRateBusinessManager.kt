package com.psk.sixminutes

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.like.ble.central.util.PermissionUtils
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
    private val heartRateRepository = DeviceRepositoryManager.createBleDeviceRepository<HeartRateRepository>(DeviceType.HeartRate)
    private var heartRateJob: Job? = null
    private val isInitialized = AtomicBoolean(false)

    fun init(context: Context, name: String, address: String) {
        if (isInitialized.compareAndSet(false, true)) {
            heartRateRepository.init(context, name, address)
        }
    }

    fun getSampleRate(): Int {
        checkInit()
        return heartRateRepository.getSampleRate()
    }

    fun connect(activity: ComponentActivity, onResult: (List<List<Float>>) -> Unit) {
        checkInit()
        activity.lifecycleScope.launch {
            PermissionUtils.requestConnectEnvironment(activity)
            heartRateRepository.connect(activity.lifecycleScope, 0L, {
                activity.showToast("心电仪连接成功，开始测量")
                heartRateJob = activity.lifecycleScope.launch {
                    heartRateRepository.fetch().filterNotNull().map {
                        it.coorYValues
                    }.buffer(Int.MAX_VALUE).collect {
                        // 取反，因为如果不处理，画出的波形图是反的
                        onResult(listOf(it.map { -it }))
                    }
                }
            }) {
                activity.showToast("心电仪连接失败，无法进行测量")
                heartRateJob?.cancel()
                heartRateJob = null
            }
        }
    }

    fun disconnect() {
        checkInit()
        heartRateJob?.cancel()
        heartRateJob = null
        heartRateRepository.close()
    }

    private fun checkInit() {
        check(isInitialized.get()) {
            "请先调用 init() 方法进行初始化"
        }
    }

}