package com.psk.shangxiazhi.train

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.like.common.util.mvi.Event
import com.psk.ble.DeviceType
import com.psk.common.util.ToastEvent
import com.psk.device.DeviceManager
import com.psk.device.data.model.HealthInfo
import com.psk.shangxiazhi.data.model.ShangXiaZhiReport
import com.psk.shangxiazhi.game.GameManagerService
import com.psk.shangxiazhi.report.ReportActivity
import com.psk.shangxiazhi.scene.SceneActivity
import com.twsz.twsystempre.TrainScene
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

@OptIn(KoinApiExtension::class)
class TrainViewModel(deviceManager: DeviceManager) : ViewModel(), KoinComponent {
    private val _uiState = MutableStateFlow(TrainUiState())
    val uiState = _uiState.asStateFlow()
    private val healthInfoRepository = deviceManager.healthInfoRepository

    //创建一个ServiceConnection回调，通过IBinder进行交互
    private val localConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.w(TAG, "onServiceDisconnected")
            //本句话借用：Android系统在同service的连接意外丢失时调用这个．比如当service崩溃了或被强杀了．当客户端解除绑定时，这个方法不会被调用．
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.w(TAG, "onServiceConnected")
            val localBinder = service as? GameManagerService.LocalBinder
            _uiState.update {
                it.copy(gameManagerService = localBinder?.getService())
            }
        }
    }

    fun bindGameManagerService(context: Context) {
        Log.w(TAG, "bindGameManagerService")
        val intent = Intent(context, GameManagerService::class.java)
        context.bindService(intent, localConnection, AppCompatActivity.BIND_AUTO_CREATE)
    }

    fun unbindGameManagerService(context: Context) {
        try {
            Log.w(TAG, "unbindGameManagerService")
            context.unbindService(localConnection)
        } catch (e: Exception) {
        }
    }

    fun selectDevices(activity: FragmentActivity) {
        SelectDeviceDialogFragment.newInstance(
            arrayOf(
                DeviceType.ShangXiaZhi,
                DeviceType.BloodOxygen,
                DeviceType.BloodPressure,
                DeviceType.HeartRate,
            )
        ).apply {
            onSelected = { deviceMap ->
                _uiState.update {
                    it.copy(
                        deviceMap = deviceMap,
                        healthInfo = HealthInfo(),
                        scene = null,
                        reports = null
                    )
                }
            }
        }.show(activity)
    }

    fun selectTrainScene(activity: ComponentActivity) {
        SceneActivity.start(activity) { activityResult ->
            if (activityResult.resultCode != Activity.RESULT_OK) {
                return@start
            }
            _uiState.update {
                it.copy(
                    scene = activityResult.data?.getSerializableExtra(SceneActivity.KEY_SCENE) as? TrainScene
                )
            }
        }
    }

    fun setPersonInfo(activity: FragmentActivity) {
        PersonInfoDialogFragment.newInstance().apply {
            onSelected = { age, weight ->
                _uiState.update {
                    it.copy(
                        healthInfo = it.healthInfo?.copy(
                            age = age, weight = weight
                        ),
                    )
                }
            }
        }.show(activity)
    }

    fun measureTargetHeart(activity: FragmentActivity) {
        val healthInfo = _uiState.value.healthInfo
        if (healthInfo == null || healthInfo.age == 0) {
            _uiState.update {
                it.copy(
                    toastEvent = Event(ToastEvent(text = "请填写基本信息中的年龄"))
                )
            }
            return
        }
        val deviceMap = _uiState.value.deviceMap
        val bleSanInfo = deviceMap?.get(DeviceType.HeartRate) ?: return
        MeasureTargetHeartRateDialogFragment.newInstance(healthInfo.age, bleSanInfo.name, bleSanInfo.address).apply {
            onSelected = { minTargetHeartRate: Int, maxTargetHeartRate: Int ->
                _uiState.update {
                    it.copy(
                        healthInfo = it.healthInfo?.copy(
                            minTargetHeartRate = minTargetHeartRate, maxTargetHeartRate = maxTargetHeartRate
                        ),
                    )
                }
            }
        }.show(activity)
    }

    fun measureBloodPressureBefore(activity: FragmentActivity) {
        val deviceMap = _uiState.value.deviceMap
        val bleSanInfo = deviceMap?.get(DeviceType.BloodPressure) ?: return
        MeasureBloodPressureDialogFragment.newInstance(bleSanInfo.name, bleSanInfo.address).apply {
            onSelected = { bloodPressure ->
                _uiState.update {
                    it.copy(
                        healthInfo = it.healthInfo?.copy(
                            bloodPressureBefore = bloodPressure
                        ),
                    )
                }
            }
        }.show(activity)
    }

    fun measureBloodPressureAfter(activity: FragmentActivity) {
        val reports = _uiState.value.reports
        if (reports.isNullOrEmpty()) {
            _uiState.update {
                it.copy(
                    toastEvent = Event(ToastEvent(text = "请先进行训练"))
                )
            }
            return
        }
        val deviceMap = _uiState.value.deviceMap
        val bleSanInfo = deviceMap?.get(DeviceType.BloodPressure) ?: return
        MeasureBloodPressureDialogFragment.newInstance(bleSanInfo.name, bleSanInfo.address).apply {
            onSelected = { bloodPressure ->
                _uiState.update {
                    it.copy(
                        healthInfo = it.healthInfo?.copy(
                            bloodPressureAfter = bloodPressure
                        ),
                    )
                }
            }
        }.show(activity)
    }

    fun train() {
        val deviceMap = _uiState.value.deviceMap
        if (deviceMap.isNullOrEmpty()) {
            _uiState.update {
                it.copy(
                    toastEvent = Event(ToastEvent(text = "请先选择设备"))
                )
            }
            return
        }
        val scene = _uiState.value.scene
        if (scene == null) {
            _uiState.update {
                it.copy(
                    toastEvent = Event(ToastEvent(text = "请选择游戏场景"))
                )
            }
            return
        }
        val healthInfo = _uiState.value.healthInfo
        val weight = healthInfo?.weight
        if (weight == null || weight == 0) {
            _uiState.update {
                it.copy(
                    toastEvent = Event(ToastEvent(text = "请填写基本信息中的体重"))
                )
            }
            return
        }
        val medicalOrderId = System.currentTimeMillis()
        _uiState.update {
            it.copy(
                healthInfo = it.healthInfo?.copy(
                    medicalOrderId = medicalOrderId
                ),
            )
        }
        _uiState.value.gameManagerService?.start(medicalOrderId, deviceMap, scene) { reports ->
            _uiState.update {
                it.copy(
                    reports = reports
                )
            }
        }
        return
    }

    fun report() {
        val deviceMap = _uiState.value.deviceMap
        if (deviceMap.isNullOrEmpty()) {
            _uiState.update {
                it.copy(
                    toastEvent = Event(ToastEvent(text = "请先进行训练"))
                )
            }
            return
        }
        val reports = _uiState.value.reports
        val shangXiaZhiReport = reports?.firstOrNull {
            it is ShangXiaZhiReport
        } as? ShangXiaZhiReport
        if (shangXiaZhiReport == null) {
            _uiState.update {
                it.copy(
                    toastEvent = Event(ToastEvent(text = "请先进行训练"))
                )
            }
            return
        }
        val healthInfo = _uiState.value.healthInfo
        if (healthInfo == null) {
            _uiState.update {
                it.copy(
                    toastEvent = Event(ToastEvent(text = "请先进行训练"))
                )
            }
            return
        }
        val cal = shangXiaZhiReport.activeCal + shangXiaZhiReport.passiveCal
        val duration = shangXiaZhiReport.activeDuration + shangXiaZhiReport.passiveDuration
        val met = if (duration == 0 || healthInfo.weight == 0) {
            0
        } else {
            (cal / duration / healthInfo.weight / 0.0167f).toInt()
        }
        val newHealthInfo = healthInfo.copy(
            met = met
        )
        _uiState.update {
            it.copy(
                healthInfo = newHealthInfo,
            )
        }
        viewModelScope.launch {
            healthInfoRepository.insertOrUpdate(newHealthInfo)
        }
        ReportActivity.start(reports, newHealthInfo)
    }

    companion object {
        private val TAG = TrainViewModel::class.java.simpleName
    }

}
