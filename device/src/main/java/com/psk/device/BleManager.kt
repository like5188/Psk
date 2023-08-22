package com.psk.device

import android.content.Context
import androidx.activity.ComponentActivity
import com.like.ble.central.scan.result.ScanResult
import com.like.ble.util.BleBroadcastReceiverManager
import com.like.ble.util.PermissionUtils
import com.like.ble.util.hexStringToByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class BleManager(private val context: Context) {
    private val bleBroadcastReceiverManager by lazy {
        BleBroadcastReceiverManager(context, onBleOn = {}, onBleOff = {
            onTip?.invoke(Error("蓝牙已关闭"))
        })
    }
    private val scanManager by lazy {
        ScanManager(context).apply {
            this.onTip = this@BleManager.onTip
        }
    }
    private val connectManagers = mutableMapOf<Device, ConnectManager>()
    var onTip: ((Tip) -> Unit)? = null

    init {
        bleBroadcastReceiverManager.register()
    }

    suspend fun init(activity: ComponentActivity) {
        PermissionUtils.requestScanEnvironment(activity)
        PermissionUtils.requestConnectEnvironment(activity)
    }

    fun addDevices(vararg devices: Device) {
        devices.forEach {
            if (!connectManagers.containsKey(it)) {
                connectManagers[it] = ConnectManager(context, it).apply {
                    this.onTip = this@BleManager.onTip
                }
            }
        }
    }

    /**
     * 连接添加的所有蓝牙设备。
     */
    fun connectAll(
        scope: CoroutineScope,
        autoConnectInterval: Long,
        onConnected: (Device) -> Unit,
        onDisconnected: (Device) -> Unit
    ) {
        if (connectManagers.isEmpty()) {
            onTip?.invoke(Normal("connectAll 请先调用 addDevices 方法添加设备。"))
            return
        }
        connectManagers.values.forEach {
            it.connect(scope, autoConnectInterval, onConnected, onDisconnected)
        }
    }

    /**
     * 扫描蓝牙设备。
     */
    fun scan(): Flow<ScanResult> {
        return scanManager.startScan()
    }

    fun stopScan() {
        scanManager.stopScan()
    }

    fun isAllDeviceConnected(): Boolean {
        if (connectManagers.isEmpty()) {
            return false
        }
        return connectManagers.values.all {
            it.isConnected()
        }
    }

    fun isConnected(deviceType: DeviceType): Boolean {
        return connectManagers.any {
            it.key.type == deviceType && it.value.isConnected()
        }
    }

    fun isConnected(device: Device): Boolean {
        return connectManagers.getOrDefault(device, null)?.isConnected() == true
    }

    fun setNotifyCallback(device: Device): Flow<ByteArray>? {
        val connectManager = connectManagers[device]
        if (connectManager == null) {
            onTip?.invoke(Error("setNotifyCallback 未找到设备：${device.address}"))
            return null
        }
        return connectManager.setNotifyCallback()
    }

    suspend fun write(device: Device, cmd: String) {
        write(device, cmd.hexStringToByteArray())
    }

    suspend fun write(device: Device, cmd: ByteArray) {
        val connectManager = connectManagers[device]
        if (connectManager == null) {
            onTip?.invoke(Error("write 未找到设备：${device.address}"))
            return
        }
        connectManager.write(cmd)
    }

    suspend fun writeAndWaitResult(device: Device, cmd: String): ByteArray? {
        return writeAndWaitResult(device, cmd.hexStringToByteArray())
    }

    suspend fun writeAndWaitResult(device: Device, cmd: ByteArray): ByteArray? {
        val connectManager = connectManagers[device]
        if (connectManager == null) {
            onTip?.invoke(Error("writeAndWaitResult 未找到设备：${device.address}"))
            return null
        }
        return connectManager.writeAndWaitResult(cmd)
    }

    suspend fun waitResult(device: Device): ByteArray? {
        val connectManager = connectManagers[device]
        if (connectManager == null) {
            onTip?.invoke(Error("waitResult 未找到设备：${device.address}"))
            return null
        }
        return connectManager.writeAndWaitResult(null)
    }

    fun onDestroy() {
        bleBroadcastReceiverManager.unregister()
        connectManagers.values.forEach {
            it.close()
        }
        connectManagers.clear()
        scanManager.close()
    }

}
