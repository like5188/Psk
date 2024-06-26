package com.psk.device.data.source.repository.ble

import com.psk.device.data.db.DeviceDatabaseManager
import com.psk.device.data.model.BloodPressure
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.local.BloodPressureDbDataSource
import com.psk.device.data.source.remote.ble.base.BaseBloodPressureDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * 血压数据仓库
 */
class BloodPressureRepository : BaseBleDeviceRepository<BaseBloodPressureDataSource>(DeviceType.BloodPressure) {
    private val dbDataSource by lazy {
        BloodPressureDbDataSource(DeviceDatabaseManager.db.bloodPressureDao())
    }

    suspend fun getListByOrderId(orderId: Long): List<BloodPressure>? {
        return dbDataSource.getByOrderId(orderId)
    }

    /**
     * 注意返回的是热流
     */
    fun getFetchFlow(scope: CoroutineScope, orderId: Long, interval: Long): Flow<BloodPressure> {
        scope.launch(Dispatchers.IO) {
            while (isActive) {
                bleDeviceDataSource.fetch(orderId)?.apply {
                    dbDataSource.insert(this)
                }
                // 设备大概在3秒内可以多次获取同一次测量结果。
                delay(interval)
            }
        }
        return dbDataSource.listenLatest(System.currentTimeMillis()).filterNotNull()
    }

    /**
     * 注意返回的是热流
     */
    fun getMeasureFlow(scope: CoroutineScope, orderId: Long, interval: Long): Flow<BloodPressure> {
        scope.launch(Dispatchers.IO) {
            delay(100)// 这里必须延迟一下，否则在机顶盒上，会出现连接成功开始测量失败。
            while (isActive) {
                println("开始测量血压")
                bleDeviceDataSource.measure(orderId)?.apply {
                    dbDataSource.insert(this)
                }
                println("血压测量完成")
                // 延迟，并在延迟阶段发送连接指令使血压计处于开机状态，因为测量完成后，大概1分钟血压计就会自动关机。
                val sendOrderInterval = 19 * 1000L// 这里取19秒，这样1分钟内可以发送3次，大大减少发送失败导致血压计关机的情况。
                if (interval < sendOrderInterval) {
                    delay(interval)
                } else {
                    var remain = interval
                    while (remain > 0) {
                        val d = remain.coerceAtMost(sendOrderInterval)
                        delay(d)
                        remain -= sendOrderInterval
                        if (d >= sendOrderInterval) {
                            println("发送连接指令使血压计处于开机状态：${bleDeviceDataSource.keepConnect()}")
                        }
                    }
                }
            }
        }
        return dbDataSource.listenLatest(System.currentTimeMillis()).filterNotNull()
    }

    suspend fun measure(): BloodPressure? {
        return bleDeviceDataSource.measure(-1)
    }

    suspend fun stopMeasure() {
        bleDeviceDataSource.stopMeasure()
    }

}