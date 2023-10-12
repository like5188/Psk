package com.psk.device.data.source

import com.psk.device.data.db.dao.BaseDao
import com.psk.device.data.db.database.DeviceDatabase
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.local.db.DbDataSourceFactory
import com.psk.device.data.source.local.db.IDbDataSource
import com.psk.device.data.source.remote.BleDataSourceFactory
import com.psk.device.data.source.remote.base.BaseBleDeviceDataSource
import kotlinx.coroutines.CoroutineScope
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

@OptIn(KoinApiExtension::class)
abstract class BaseBleDeviceRepository<T, BleDeviceDataSource : BaseBleDeviceDataSource>(
    private val deviceType: DeviceType
) : KoinComponent {
    protected val dbDataSource: IDbDataSource<T> by lazy {
        // 从DeviceDatabase中获取deviceType对应的方法
        val method = DeviceDatabase::class.java.declaredMethods.firstOrNull {
            it.name.lowercase().startsWith(deviceType.name.lowercase())
        }
        method?.isAccessible = true
        val packageName = BaseDao::class.java.`package`?.name
        val paramsClass = Class.forName("$packageName.${deviceType.name}Dao")
        DbDataSourceFactory.create(deviceType, method?.invoke(get<DeviceDatabase>()), paramsClass)
    }
    protected lateinit var bleDeviceDataSource: BleDeviceDataSource

    /**
     * 如果需要连接远端蓝牙设备，并且获取数据，必须调用此方法启用设备，然后才能使用相关操作。
     * 如果只是需要调用[getListByMedicalOrderId]方法需要获取数据库中缓存的数据，则不需要调用此方法。
     */
    fun enable(name: String, address: String) {
        bleDeviceDataSource = BleDataSourceFactory.create(name, deviceType)
        bleDeviceDataSource.enable(address)
    }

    suspend fun getListByMedicalOrderId(medicalOrderId: Long): List<T>? {
        return dbDataSource.getByMedicalOrderId(medicalOrderId)
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