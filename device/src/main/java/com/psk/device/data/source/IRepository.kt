package com.psk.device.data.source

import kotlinx.coroutines.CoroutineScope

interface IRepository<T> {
    /**
     * 如果需要连接远端蓝牙设备，并且获取数据，必须调用此方法添加设备，然后才能使用bleManager进行相关操作。
     * 如果只是需要获取数据库中缓存的数据，则不需要调用此方法。
     */
    fun enable(name: String, address: String)

    suspend fun getListByMedicalOrderId(medicalOrderId: Long): List<T>?

    fun connect(
        scope: CoroutineScope,
        onConnected: () -> Unit,
        onDisconnected: () -> Unit
    )

    fun isConnected(): Boolean

    fun close()

}