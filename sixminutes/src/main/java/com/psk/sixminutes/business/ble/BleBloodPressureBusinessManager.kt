package com.psk.sixminutes.business.ble

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.like.common.util.showToast
import com.psk.device.DeviceRepositoryManager
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.repository.ble.BloodPressureRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

class BleBloodPressureBusinessManager {
    private val repository = DeviceRepositoryManager.createBleDeviceRepository<BloodPressureRepository>(DeviceType.BloodPressure)
    private var job: Job? = null
    private val isInitialized = AtomicBoolean(false)

    fun init(context: Context, name: String, address: String) {
        if (isInitialized.compareAndSet(false, true)) {
            repository.init(context, name, address)
        }
    }

    fun measure(
        context: Context,
        lifecycleScope: LifecycleCoroutineScope,
        onBloodPressureResult: (Int, Int) -> Unit,
    ) {
        checkInit()
        if (repository.isConnected()) {
            startJob(context, lifecycleScope, onBloodPressureResult)
        } else {
            lifecycleScope.launch {
                repository.connect(lifecycleScope, 0L, {
                    context.showToast("血压仪连接成功，开始测量")
                    startJob(context, lifecycleScope, onBloodPressureResult)
                }) {
                    context.showToast("血压仪连接失败，无法进行测量")
                    job?.cancel()
                    job = null
                }
            }
        }
    }

    suspend fun stopMeasure() = withContext(Dispatchers.IO) {
        job?.cancelAndJoin()// 这里必须等待上一条命令执行完毕，否则会导致stopMeasure失败
        job = null
        delay(100)
        repository.stopMeasure()
    }

    private fun startJob(context: Context, lifecycleScope: LifecycleCoroutineScope, onBloodPressureResult: (Int, Int) -> Unit) {
        if (job != null) {
            context.showToast("正在测量，请稍后")
            return
        }
        job = lifecycleScope.launch {
            delay(100)// 这里延迟一下，避免刚连接成功就测量返回null值。
            val bloodPressure = repository.measure()
            if (bloodPressure == null) {
                context.showToast("测量失败，请重新测量！")
            } else {
                onBloodPressureResult(bloodPressure.sbp, bloodPressure.dbp)
            }
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