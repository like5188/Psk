package com.psk.sixminutes.business.ble

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.like.common.util.showToast
import com.psk.common.customview.CountDownTimerProgressDialog
import com.psk.common.customview.ProgressDialog
import com.psk.device.DeviceRepositoryManager
import com.psk.device.data.model.BloodPressure
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

    fun init(activity: ComponentActivity, name: String, address: String) {
        if (isInitialized.compareAndSet(false, true)) {
            this.context = activity.applicationContext
            this.lifecycleScope = activity.lifecycleScope
            repository.init(context, name, address)
            mProgressDialog = ProgressDialog(activity, "正在连接血压仪，请稍后……")
            mCountDownTimerProgressDialog =
                CountDownTimerProgressDialog(activity, "正在测量血压，请稍后！", countDownTime = 0L, onCanceled = {
                    stopMeasure()
                })
        }
    }

    fun measure(onBloodPressureResult: (BloodPressure) -> Unit) {
        checkInit()
        if (repository.isConnected()) {
            startJob(context, lifecycleScope, onBloodPressureResult)
        } else {
            mProgressDialog.show()
            repository.connect(lifecycleScope, onConnected = {
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

    fun stopMeasure() = lifecycleScope.launch(Dispatchers.IO) {
        job?.cancelAndJoin()// 这里必须等待上一条命令执行完毕，否则会导致stopMeasure失败
        job = null
        delay(100)
        repository.stopMeasure()
    }

    private fun startJob(context: Context, lifecycleScope: LifecycleCoroutineScope, onBloodPressureResult: (BloodPressure) -> Unit) {
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
                onBloodPressureResult(bloodPressure)
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