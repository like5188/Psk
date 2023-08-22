package com.psk.device

import android.annotation.SuppressLint
import android.content.Context
import com.like.ble.central.connect.executor.ConnectExecutorFactory
import com.like.ble.exception.BleException
import com.like.ble.exception.BleExceptionBusy
import com.like.ble.exception.BleExceptionCancelTimeout
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

internal class ConnectManager(private val context: Context, private val device: Device) {
    private val connectExecutor by lazy {
        ConnectExecutorFactory.get(context, device.address)
    }
    var onTip: ((Tip) -> Unit)? = null

    fun isConnected(): Boolean {
        return connectExecutor.isBleDeviceConnected()
    }

    @SuppressLint("MissingPermission")
    fun connect(
        scope: CoroutineScope,
        autoConnectInterval: Long,
        onConnected: (Device) -> Unit,
        onDisconnected: (Device) -> Unit
    ) {
        connectExecutor.connect(scope, autoConnectInterval, onConnected = { device, gattServiceList ->
            onConnected(this.device.apply { name = device.name ?: "" })
            onTip?.invoke(Normal("连接成功：${device.name} ${device.address}"))
        }) {
            when (it) {
                is BleExceptionCancelTimeout -> {
                    // 提前取消超时(BleExceptionCancelTimeout)不做处理。因为这是调用 disconnect() 造成的，使用者可以直接在 disconnect() 方法结束后处理 UI 的显示，不需要此回调。
                }

                is BleExceptionBusy -> {
                    onTip?.invoke(Error(it))
                }

                else -> {
                    onTip?.invoke(Error(it))
                    onDisconnected(device)
                }
            }
        }
    }

    fun setNotifyCallback(): Flow<ByteArray> =
        connectExecutor.setCharacteristicNotificationAndNotifyCallback(device.protocol.notifyUUID, device.protocol.serviceUUID).catch {
            when (it) {
                is BleExceptionCancelTimeout -> {
                    // 提前取消超时(BleExceptionCancelTimeout)不做处理。因为这是调用 disconnect() 造成的，使用者可以直接在 disconnect() 方法结束后处理 UI 的显示，不需要此回调。
                }

                is BleExceptionBusy -> {
                }

                else -> {
                    onTip?.invoke(Error("setNotifyCallback 失败：${it.message}"))
                }
            }
        }

    suspend fun write(cmd: ByteArray) {
        try {
            connectExecutor.writeCharacteristic(cmd, device.protocol.writeUUID, device.protocol.serviceUUID)
        } catch (e: BleException) {
            when (e) {
                is BleExceptionCancelTimeout -> {
                    // 提前取消超时(BleExceptionCancelTimeout)不做处理。因为这是调用 disconnect() 造成的，使用者可以直接在 disconnect() 方法结束后处理 UI 的显示，不需要此回调。
                }

                is BleExceptionBusy -> {
                }

                else -> {
                    onTip?.invoke(Error("write 失败：${e.message}"))
                }
            }
        }
    }

    /**
     * 写特征值并等待通知
     *
     * @param cmd   命令。如果为 null 或者空，则不会写入命令，只是等待通知
     */
    suspend fun writeAndWaitResult(cmd: ByteArray?): ByteArray? = try {
        val isBeginOfPacket = device.protocol.isBeginOfPacket ?: throw IllegalArgumentException("isStart in protocol is null")
        val isFullPacket = device.protocol.isFullPacket ?: throw IllegalArgumentException("isWhole in protocol is null")
        connectExecutor.writeCharacteristicAndWaitNotify(
            cmd,
            device.protocol.writeUUID,
            device.protocol.notifyUUID,
            device.protocol.serviceUUID,
            timeout = 60000L,
            isBeginOfPacket = isBeginOfPacket,
            isFullPacket = isFullPacket
        )
    } catch (e: BleException) {
        when (e) {
            is BleExceptionCancelTimeout -> {
                // 提前取消超时(BleExceptionCancelTimeout)不做处理。因为这是调用 disconnect() 造成的，使用者可以直接在 disconnect() 方法结束后处理 UI 的显示，不需要此回调。
            }

            is BleExceptionBusy -> {
            }

            else -> {
                onTip?.invoke(Error("writeAndWaitResult 失败：${e.message}"))
            }
        }
        null
    }

    fun close() {
        connectExecutor.close()
    }

}
