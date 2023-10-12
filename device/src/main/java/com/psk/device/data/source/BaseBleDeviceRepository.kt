package com.psk.device.data.source

import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.remote.BleDataSourceFactory
import com.psk.device.data.source.remote.base.BaseBleDeviceDataSource
import kotlinx.coroutines.CoroutineScope

abstract class BaseBleDeviceRepository<BleDeviceDataSource : BaseBleDeviceDataSource>(
    private val deviceType: DeviceType
) {
    protected lateinit var bleDeviceDataSource: BleDeviceDataSource

    /**
     * 如果需要连接远端蓝牙设备，并且获取数据，必须调用此方法启用设备，然后才能使用相关操作。
     * 如果只是需要调用[getListByMedicalOrderId]方法需要获取数据库中缓存的数据，则不需要调用此方法。
     */
    fun enable(name: String, address: String) {
        // 根据name反射创建对应数据源，因为每种设备类型可能对应多个厂商的不同设备，所以这里使用反射来简化，便于扩展
        bleDeviceDataSource = BleDataSourceFactory.create(name, deviceType)
        bleDeviceDataSource.enable(address)
    }

    fun connect(scope: CoroutineScope, onConnected: () -> Unit, onDisconnected: () -> Unit) {
        bleDeviceDataSource.connect(scope, onConnected, onDisconnected)
    }

    fun isConnected(): Boolean {
        return bleDeviceDataSource.isConnected()
    }

    fun close() {
        bleDeviceDataSource.close()
    }

}