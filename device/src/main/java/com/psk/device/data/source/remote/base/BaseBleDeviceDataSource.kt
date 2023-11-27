package com.psk.device.data.source.remote.base

import android.annotation.SuppressLint
import android.content.Context
import com.like.ble.central.connect.executor.AbstractConnectExecutor
import com.like.ble.central.connect.executor.ConnectExecutorFactory
import com.like.ble.exception.BleException
import com.like.ble.exception.BleExceptionBusy
import com.like.ble.exception.BleExceptionCancelTimeout
import com.like.ble.util.toByteArray
import com.like.common.util.Logger
import com.psk.device.data.model.Protocol
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

abstract class BaseBleDeviceDataSource {
    protected lateinit var address: String
    abstract val protocol: Protocol
    private lateinit var connectExecutor: AbstractConnectExecutor

    fun init(context: Context, address: String) {
        this.address = address
        connectExecutor = ConnectExecutorFactory.get(context, address)
    }

    fun isConnected(): Boolean {
        return try {
            connectExecutor.isBleDeviceConnected()
        } catch (e: Exception) {
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun connect(
        scope: CoroutineScope,
        autoConnectInterval: Long,
        onConnected: () -> Unit,
        onDisconnected: () -> Unit
    ) {
        connectExecutor.connect(scope, autoConnectInterval, onConnected = { device, gattServiceList ->
            onConnected()
        }) {
            when (it) {
                is BleExceptionCancelTimeout -> {
                    // 提前取消超时(BleExceptionCancelTimeout)不做处理。因为这是调用 disconnect() 造成的，使用者可以直接在 disconnect() 方法结束后处理 UI 的显示，不需要此回调。
                }

                is BleExceptionBusy -> {
                    Logger.w("connect 失败：${it.message}")
                }

                else -> {
                    Logger.e("connect 失败：${it.message}")
                    onDisconnected()
                }
            }
        }
    }

    @Throws(BleExceptionBusy::class)
    fun setNotifyCallback(): Flow<ByteArray> =
        connectExecutor.setCharacteristicNotificationAndNotifyCallback(protocol.notifyUUID, protocol.serviceUUID).catch {
            when (it) {
                is BleExceptionCancelTimeout -> {
                    // 提前取消超时(BleExceptionCancelTimeout)不做处理。因为这是调用 disconnect() 造成的，使用者可以直接在 disconnect() 方法结束后处理 UI 的显示，不需要此回调。
                }

                is BleExceptionBusy -> {
                    Logger.w("setNotifyCallback 失败：${it.message}")
                    throw it
                }

                else -> {
                    Logger.e("setNotifyCallback 失败：${it.message}")
                }
            }
        }

    suspend fun write(cmd: String): Boolean {
        return write(cmd.toByteArray())
    }

    suspend fun write(cmd: ByteArray): Boolean = try {
        connectExecutor.writeCharacteristic(cmd, protocol.writeUUID, protocol.serviceUUID)
        true
    } catch (e: BleException) {
        when (e) {
            is BleExceptionCancelTimeout -> {
                // 提前取消超时(BleExceptionCancelTimeout)不做处理。因为这是调用 disconnect() 造成的，使用者可以直接在 disconnect() 方法结束后处理 UI 的显示，不需要此回调。
            }

            is BleExceptionBusy -> {
                Logger.w("write 失败：${e.message}")
            }

            else -> {
                Logger.e("write 失败：${e.message}")
            }
        }
        false
    }

    suspend fun writeAndWaitResult(cmd: String?): ByteArray? {
        return writeAndWaitResult(cmd.toByteArray())
    }

    /**
     * 写特征值并等待通知
     *
     * @param cmd   命令。如果为 null 或者空，则不会写入命令，只是等待通知
     */
    suspend fun writeAndWaitResult(cmd: ByteArray?): ByteArray? = try {
        val isBeginOfPacket = protocol.isBeginOfPacket ?: throw IllegalArgumentException("isStart in protocol is null")
        val isFullPacket = protocol.isFullPacket ?: throw IllegalArgumentException("isWhole in protocol is null")
        connectExecutor.writeCharacteristicAndWaitNotify(
            cmd,
            protocol.writeUUID,
            protocol.notifyUUID,
            protocol.serviceUUID,
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
                Logger.w("writeAndWaitResult 失败：${e.message}")
            }

            else -> {
                Logger.e("writeAndWaitResult 失败：${e.message}")
            }
        }
        null
    }

    fun close() {
        try {
            connectExecutor.close()
        } catch (e: Exception) {
        }
    }
}