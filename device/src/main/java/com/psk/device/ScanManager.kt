package com.psk.device

import android.annotation.SuppressLint
import android.content.Context
import com.like.ble.central.scan.executor.AbstractScanExecutor
import com.like.ble.central.scan.executor.ScanExecutorFactory
import com.like.ble.central.scan.result.ScanResult
import com.like.ble.exception.BleExceptionBusy
import com.like.ble.exception.BleExceptionCancelTimeout
import com.like.ble.exception.BleExceptionTimeout
import com.like.common.util.Logger
import com.psk.device.ScanManager.init
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.remote.BleDataSourceFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.filter

/**
 * 扫描工具类。
 * 1、调用[init]进行初始化
 * 2、使用其中的方法。
 */
object ScanManager {
    @SuppressLint("StaticFieldLeak")
    private lateinit var scanExecutor: AbstractScanExecutor

    suspend fun init(context: Context) {
        scanExecutor = ScanExecutorFactory.get(context)
        /**
         * [BleDataSourceFactory.init]必须放在扫描之前，否则扫描时，如果要用到[DeviceType.containsDevice]方法就没效果。
         */
        BleDataSourceFactory.init(context)
    }

    /**
     * 判断当前设备类型是否包含指定设备名称[name]的设备
     */
    private fun DeviceType.containsDevice(name: String): Boolean {
        if (name.isEmpty()) {
            return false
        }
        BleDataSourceFactory.foreach { prefix, deviceTypeName, clazz ->
            if (deviceTypeName == this.name && name.startsWith(prefix)) {
                return true
            }
        }
        return false
    }

    /**
     * 扫描指定设备类型的设备
     */
    @SuppressLint("MissingPermission")
    fun startScan(deviceType: DeviceType): Flow<ScanResult> = scanExecutor.startScan().catch {
        when (it) {
            is BleExceptionCancelTimeout -> {
                // 提前取消超时不做处理。因为这是调用 stopScan() 造成的，使用者可以直接在 stopScan() 方法结束后处理 UI 的显示，不需要此回调。
            }

            is BleExceptionBusy -> {
                // 扫描中
                Logger.w("扫描 失败：${it.message}")
            }

            is BleExceptionTimeout -> {
                // 扫描完成
            }

            else -> {
                // 扫描出错
                Logger.e("扫描 失败：${it.message}")
            }
        }
    }.conflate()// 如果消费者还在处理，则丢弃新的数据。然后消费者处理完后，再去获取生产者中的最新数据来处理。
        .filter {
            val name = it.device.name
            val address = it.device.address
            !name.isNullOrEmpty() && !address.isNullOrEmpty() && deviceType.containsDevice(name)
        }

    fun stopScan() {
        try {
            scanExecutor.stopScan()
        } catch (e: Exception) {
        }
    }

    fun close() {
        try {
            scanExecutor.close()
        } catch (e: Exception) {
        }
    }

}