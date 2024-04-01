package com.psk.device.socket

import com.psk.device.data.model.DeviceType
import com.psk.device.socket.remote.base.BaseSocketDeviceDataSource
import com.psk.device.socket.remote.SocketDataSourceFactory
import kotlinx.coroutines.CoroutineScope

abstract class BaseSocketDeviceRepository<SocketDeviceDataSource : BaseSocketDeviceDataSource>(
    private val deviceType: DeviceType
) {
    protected lateinit var socketDeviceDataSource: SocketDeviceDataSource

    /**
     * 如果需要连接远端蓝牙设备，并且获取数据，必须调用此方法初始化设备，然后才能使用其它相关操作。
     * 如果只是需要调用[getListByOrderId]方法获取数据库中缓存的数据，则不需要调用此方法。
     */
    fun init(name: String, hostName: String?, port: Int) {
        // 根据name反射创建对应数据源，因为每种设备类型可能对应多个厂商的不同设备，所以这里使用反射来简化，便于扩展
        socketDeviceDataSource = SocketDataSourceFactory.create(name, deviceType)
        socketDeviceDataSource.init(hostName, port)
    }

    fun connect(
        scope: CoroutineScope,
        onOpen: ((address: String?) -> Unit)? = null,
        onClose: ((code: Int, reason: String?) -> Unit)? = null,
        onError: ((e: Exception?) -> Unit)? = null,
    ) {
        socketDeviceDataSource.connect(scope, onOpen, onClose, onError)
    }

    fun isConnected(): Boolean {
        return socketDeviceDataSource.isConnected()
    }

    fun close() {
        socketDeviceDataSource.close()
    }

}