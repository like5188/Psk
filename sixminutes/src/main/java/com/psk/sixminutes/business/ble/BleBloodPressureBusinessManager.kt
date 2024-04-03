package com.psk.sixminutes.business.ble

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import com.like.common.util.showToast
import com.psk.common.customview.CountDownTimerProgressDialog
import com.psk.common.customview.ProgressDialog
import com.psk.device.DeviceRepositoryManager
import com.psk.device.data.model.DeviceType
import com.psk.device.data.source.repository.ble.BloodPressureRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class BleBloodPressureBusinessManager {
    private val repository = DeviceRepositoryManager.createBleDeviceRepository<BloodPressureRepository>(DeviceType.BloodPressure)
    private var job: Job? = null
    private val isInitialized = AtomicBoolean(false)
    private lateinit var mProgressDialog: ProgressDialog
    private lateinit var mCountDownTimerProgressDialog: CountDownTimerProgressDialog
    private lateinit var lifecycleScope: LifecycleCoroutineScope
    private lateinit var context: Context

    fun init(context: Context, lifecycleScope: LifecycleCoroutineScope, name: String, address: String) {
        if (isInitialized.compareAndSet(false, true)) {
            this.context = context
            this.lifecycleScope = lifecycleScope
            repository.init(context, name, address)
            mProgressDialog = ProgressDialog(context, "正在连接血压仪，请稍后……")
            mCountDownTimerProgressDialog = CountDownTimerProgressDialog(context, "正在测量血压，请稍后！", countDownTime = 0L, onCanceled = {
                lifecycleScope.launch(Dispatchers.IO) {
                    job?.cancelAndJoin()// 这里必须等待上一条命令执行完毕，否则会导致stopMeasure失败
                    job = null
                    delay(100)
                    repository.stopMeasure()
                }
            })
        }
    }

    fun measure(onBloodPressureResult: (Int, Int) -> Unit) {
        checkInit()
        if (repository.isConnected()) {
            startJob(context, lifecycleScope, onBloodPressureResult)
        } else {
            mProgressDialog.show()
            lifecycleScope.launch {
                repository.connect(lifecycleScope, 0L, {
                    mProgressDialog.dismiss()
                    context.showToast("血压仪连接成功，开始测量")
                    startJob(context, lifecycleScope, onBloodPressureResult)
                }) {
                    mCountDownTimerProgressDialog.dismiss()
                    mProgressDialog.dismiss()
                    context.showToast("血压仪连接失败，无法进行测量")
                    job?.cancel()
                    job = null
                }
            }
        }
    }

    fun stopMeasure() = lifecycleScope.launch(Dispatchers.IO) {
        job?.cancelAndJoin()// 这里必须等待上一条命令执行完毕，否则会导致stopMeasure失败
        job = null
        delay(100)
        repository.stopMeasure()
    }

    private fun startJob(context: Context, lifecycleScope: LifecycleCoroutineScope, onBloodPressureResult: (Int, Int) -> Unit) {
        if (job != null) {
            context.showToast("正在测量，请稍后")
            return
        }
        job = lifecycleScope.launch(Dispatchers.Main) {
            mCountDownTimerProgressDialog.show()
            delay(100)// 这里延迟一下，避免刚连接成功就测量返回null值。
            val bloodPressure = repository.measure()
            mCountDownTimerProgressDialog.dismiss()
            if (bloodPressure == null) {
                context.showToast("测量失败，请重新测量！")
            } else {
                onBloodPressureResult(bloodPressure.sbp, bloodPressure.dbp)
            }
            job?.cancel()
            job = null
        }
    }

    fun disconnect() {
        job?.cancel()
        job = null
        // 避免未初始化时调用报错
        try {
            repository.close()
        } catch (e: Exception) {
        }
    }

    private fun checkInit() {
        check(isInitialized.get()) {
            "请先调用 init() 方法进行初始化"
        }
    }

}