package com.psk.sixminutes.business.socket

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.like.common.util.showToast
import com.psk.device.DeviceRepositoryManager
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.repository.socket.HeartRateRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class SocketHeartRateBusinessManager {
    private val repository = DeviceRepositoryManager.createSocketDeviceRepository<HeartRateRepository>(DeviceType.HeartRate)
    private var job: Job? = null
    private val isInitialized = AtomicBoolean(false)
    private lateinit var lifecycleScope: LifecycleCoroutineScope
    private lateinit var context: Context

    fun init(activity: ComponentActivity, name: String, hostName: String?, port: Int) {
        if (isInitialized.compareAndSet(false, true)) {
            this.context = activity.applicationContext
            this.lifecycleScope = activity.lifecycleScope
            repository.init(name, hostName, port)
        }
    }

    fun getSampleRate(): Int {
        checkInit()
        return repository.getSampleRate()
    }

    fun start(
        orderId: Long,
        onStatus: (String) -> Unit,
        onHeartRateResult: (Int) -> Unit,
        onEcgResult: (List<List<Float>>) -> Unit
    ) {
        checkInit()
        repository.start(
            onStart = {
                context.showToast("心电仪服务器启动成功，等待客户端连接……")
            },
            onOpen = { address ->
                onStatus("已连接")
                context.showToast("心电仪连接成功")
                val flow = repository.getFlow(lifecycleScope, orderId).filterNotNull()
                job = lifecycleScope.launch {
                    launch {
                        flow.map {
                            it.value
                        }.distinctUntilChanged().collect { value ->
                            onHeartRateResult(value)
                        }
                    }
                    flow.map {
                        val datas = listOf(
                            mutableListOf<Float>(),
                            mutableListOf(),
                            mutableListOf(),
                            mutableListOf(),
                            mutableListOf(),
                            mutableListOf(),
                            mutableListOf(),
                            mutableListOf(),
                            mutableListOf(),
                            mutableListOf(),
                            mutableListOf(),
                            mutableListOf(),
                        )
                        it.coorYValues.forEachIndexed { index, data ->
                            datas[index % 12].add(data)
                        }
                        datas
                    }.buffer(Int.MAX_VALUE).collect {
                        onEcgResult(it)
                    }
                }
            },
            onClose = { code, reason ->
                onStatus("已断开")
                context.showToast("心电仪连接断开")
                job?.cancel()
                job = null
            },
            onError = {
                onStatus("已断开")
                context.showToast("心电仪连接异常")
                job?.cancel()
                job = null
            })
    }

    fun stop() {
        job?.cancel()
        job = null
        // 避免未初始化时调用报错
        try {
            repository.stop()
        } catch (e: Exception) {
        }
    }

    private fun checkInit() {
        check(isInitialized.get()) {
            "请先调用 init() 方法进行初始化"
        }
    }

}