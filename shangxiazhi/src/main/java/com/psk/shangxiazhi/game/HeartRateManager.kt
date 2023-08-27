package com.psk.shangxiazhi.game

import android.util.Log
import com.psk.ble.DeviceType
import com.psk.device.data.model.HeartRate
import com.psk.device.data.source.HeartRateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

@OptIn(KoinApiExtension::class)
class HeartRateManager : BaseDeviceManager<HeartRate>(), KoinComponent {
    override val repository by inject<HeartRateRepository> { parametersOf(DeviceType.HeartRate) }

    var onHeartRateDataChanged: ((heartRate: Int) -> Unit)? = null
    var onEcgDataChanged: ((coorYArray: FloatArray) -> Unit)? = null

    override suspend fun handleFlow(flow: Flow<HeartRate>) = withContext<Unit>(Dispatchers.IO) {
        Log.d(TAG, "startHeartRateJob")
        launch(Dispatchers.IO) {
            flow.filterNotNull().map {
                it.value
            }.distinctUntilChanged().collect { value ->
                onHeartRateDataChanged?.invoke(value)
            }
        }
        launch(Dispatchers.IO) {
            flow.filterNotNull().map {
                it.coorYValues
            }.buffer(Int.MAX_VALUE).collect { coorYValues ->
                // 注意：此处不能使用 onEach 进行每个数据的延迟，因为只要延迟，由于系统资源的调度损耗，延迟会比设置的值增加10多毫秒，所以延迟10多毫秒以下毫无意义，因为根本不可能达到。
                // 这也导致1秒钟时间段内，就算延迟1毫秒，实际上延迟却会达到10多毫秒，导致最多只能发射60多个数据（实际测试）。
                // 这就导致远远达不到心电仪的采样率的100多，从而会导致数据堆积越来越多，导致界面看起来会延迟很严重。
                coorYValues.toList().chunked(5).forEach {
                    // 5个一组，125多的采样率，那么1秒钟发射25组数据就好，平均每个数据需要延迟40毫秒。
                    delay(1)
                    onEcgDataChanged?.invoke(it.toFloatArray())
                }
            }
        }
    }

    companion object {
        private val TAG = HeartRateManager::class.java.simpleName
    }
}