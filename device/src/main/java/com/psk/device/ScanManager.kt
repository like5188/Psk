package com.psk.device

import android.content.Context
import com.like.ble.central.scan.executor.AbstractScanExecutor
import com.like.ble.central.scan.executor.ScanExecutorFactory
import com.like.ble.central.scan.result.ScanResult
import com.like.ble.exception.BleExceptionBusy
import com.like.ble.exception.BleExceptionCancelTimeout
import com.like.ble.exception.BleExceptionTimeout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate

internal class ScanManager(private val context: Context) {
    private val scanExecutor: AbstractScanExecutor by lazy {
        ScanExecutorFactory.get(context)
    }
    var onTip: ((Tip) -> Unit)? = null

    fun startScan(): Flow<ScanResult> {
        return scanExecutor.startScan()
            .catch {
                when (it) {
                    is BleExceptionCancelTimeout -> {
                        // 提前取消超时不做处理。因为这是调用 stopScan() 造成的，使用者可以直接在 stopScan() 方法结束后处理 UI 的显示，不需要此回调。
                    }

                    is BleExceptionBusy -> {
                        // 扫描中
                        onTip?.invoke(Error(it))
                    }

                    is BleExceptionTimeout -> {
                        // 扫描完成
                    }

                    else -> {
                        // 扫描出错
                        onTip?.invoke(Error(it))
                    }
                }
            }
            .conflate()// 如果消费者还在处理，则丢弃新的数据。然后消费者处理完后，再去获取生产者中的最新数据来处理。
    }

    fun stopScan() {
        scanExecutor.stopScan()
    }

    fun close() {
        scanExecutor.close()
    }

}