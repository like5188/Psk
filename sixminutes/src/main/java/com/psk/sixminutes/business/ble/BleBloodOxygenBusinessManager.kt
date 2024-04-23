package com.psk.sixminutes.business.ble

import android.annotation.SuppressLint
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.like.ble.exception.BleExceptionBusy
import com.like.ble.exception.BleExceptionCancelTimeout
import com.like.ble.exception.BleExceptionTimeout
import com.like.common.util.Logger
import com.like.common.util.showToast
import com.psk.device.DeviceRepositoryManager
import com.psk.device.ScanManager
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.repository.ble.BloodOxygenRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    private var name = ""
    private var address = ""

    fun init(activity: ComponentActivity, name: String, address: String) {
        if (isInitialized.compareAndSet(false, true)) {
            this.name = name
            this.address = address
            this.context = activity.applicationContext
            this.lifecycleScope = activity.lifecycleScope
            repository.init(context, name, address)
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(orderId: Long, onStatus: (String) -> Unit, onBloodOxygenResult: (Int) -> Unit) {
        checkInit()
        lifecycleScope.launch {
            // 对于6分钟的机器，连接血氧需要先扫描，然后才能连接。
            ScanManager.startScan(DeviceType.BloodOxygen) {
                println(it)
                when (it) {
                    is BleExceptionCancelTimeout -> {
                        // 提前取消超时不做处理。因为这是调用 stopScan() 造成的，使用者可以直接在 stopScan() 方法结束后处理 UI 的显示，不需要此回调。
                    }

                    is BleExceptionBusy -> {
                        // 扫描中
                        Logger.w("扫描 失败：${it.message}")
                        onStatus("已断开")
                        context.showToast("血氧仪连接失败")
                    }

                    is BleExceptionTimeout -> {
                        // 扫描完成
                        onStatus("已断开")
                        context.showToast("血氧仪连接失败")
                        delay(5000)
                        connect(orderId, onStatus, onBloodOxygenResult)
                    }

                    else -> {
                        // 扫描出错
                        Logger.e("扫描 失败：${it.message}")
                        onStatus("已断开")
                        context.showToast("血氧仪连接失败")
                        delay(5000)
                        connect(orderId, onStatus, onBloodOxygenResult)
                    }
                }
            }.collect {
                val name = it.device.name
                val address = it.device.address
                if (this@BleBloodOxygenBusinessManager.name == name && this@BleBloodOxygenBusinessManager.address == address) {// 防止重复添加
                    ScanManager.stopScan()
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
            }

        }
    }

    fun disconnect() {
        job?.cancel()
        job = null
        // 避免未初始化时调用报错
        try {
            repository.close()
            ScanManager.close()
        } catch (e: Exception) {
        }
    }

    private fun checkInit() {
        check(isInitialized.get()) {
            "请先调用 init() 方法进行初始化"
        }
    }

}