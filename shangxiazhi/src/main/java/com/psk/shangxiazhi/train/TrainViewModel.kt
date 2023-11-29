package com.psk.shangxiazhi.train

import android.annotation.SuppressLint
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
import com.like.common.util.ToastEvent
import com.like.common.util.mvi.Event
import com.psk.device.RepositoryManager
import com.psk.device.data.model.DeviceType
import com.psk.device.data.model.HealthInfo
import com.psk.device.data.model.OrderInfo
import com.psk.shangxiazhi.data.model.ShangXiaZhiReport
import com.psk.shangxiazhi.game.GameManagerService
import com.psk.shangxiazhi.measure.MeasureBloodPressureDialogFragment
import com.psk.shangxiazhi.measure.MeasureTargetHeartRateDialogFragment
import com.psk.shangxiazhi.report.ReportActivity
import com.psk.shangxiazhi.scene.SceneActivity
import com.psk.shangxiazhi.selectdevice.SelectDeviceDialogFragment
import com.twsz.twsystempre.TrainScene
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent

@OptIn(KoinApiExtension::class)
class TrainViewModel : ViewModel(), KoinComponent {
    private val _uiState = MutableStateFlow(TrainUiState())
    val uiState = _uiState.asStateFlow()
    private val orderInfoRepository = RepositoryManager.orderInfoRepository
    private val healthInfoRepository = RepositoryManager.healthInfoRepository

    @SuppressLint("StaticFieldLeak")
    private var gameManagerService: GameManagerService? = null

    //创建一个ServiceConnection回调，通过IBinder进行交互
    private val localConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName?) {
            Log.w(TAG, "onServiceDisconnected")
            //本句话借用：Android系统在同service的连接意外丢失时调用这个．比如当service崩溃了或被强杀了．当客户端解除绑定时，这个方法不会被调用．
            gameManagerService = null
        }

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.w(TAG, "onServiceConnected")
            val localBinder = service as? GameManagerService.LocalBinder
            gameManagerService = localBinder?.getService()
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
        SelectDeviceDialogFragment.newInstance(_uiState.value.deviceMap).apply {
            onSelected = { deviceMap ->
                // 选择设备后，清空当前已经存在的数据。重新开始一个新的流程。
                _uiState.update {
                    it.copy(
                        isTrainCompleted = false, deviceMap = deviceMap, healthInfo = HealthInfo(), scene = null, reports = null
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

    fun setWeight(weight: Int) {
        _uiState.update {
            it.copy(
                healthInfo = it.healthInfo?.copy(
                    weight = weight
                ),
            )
        }
    }

    fun setAge(age: Int) {
        _uiState.update {
            it.copy(
                healthInfo = it.healthInfo?.copy(
                    age = age
                ),
            )
        }
    }

    fun measureTargetHeart(activity: FragmentActivity) {
        val healthInfo = _uiState.value.healthInfo
        if (healthInfo == null || healthInfo.age < 10 || healthInfo.age > 150) {
            _uiState.update {
                it.copy(
                    toastEvent = Event(ToastEvent(text = "请先填写正确的年龄。范围：10~150"))
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

    fun measureBloodPressureAfter(activity: FragmentActivity, onCompleted: () -> Unit) {
        if (!_uiState.value.isTrainCompleted) {
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
                onCompleted()
            }
            onCanceled = onCompleted
        }.show(activity)
    }

    fun train(bloodPressureMeasureType: Int) {
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
        if (weight == null || weight < 20 || weight > 500) {
            _uiState.update {
                it.copy(
                    toastEvent = Event(ToastEvent(text = "请先填写正确的体重。范围：20~500"))
                )
            }
            return
        }
        val orderId = System.currentTimeMillis()
        val newHealthInfo = healthInfo.copy(
            orderId = orderId
        )
        _uiState.update {
            it.copy(
                healthInfo = newHealthInfo,
            )
        }
        gameManagerService?.start(viewModelScope, orderId, deviceMap, scene, bloodPressureMeasureType) { reports ->
            _uiState.update {
                it.copy(
                    isTrainCompleted = true, reports = reports
                )
            }
        }
    }

    fun report() {
        if (!_uiState.value.isTrainCompleted) {
            _uiState.update {
                it.copy(
                    toastEvent = Event(ToastEvent(text = "请先进行训练"))
                )
            }
            return
        }
        // 计算mets值
        val reports = _uiState.value.reports
        val shangXiaZhiReport = reports?.firstOrNull {
            it is ShangXiaZhiReport
        } as? ShangXiaZhiReport
        val healthInfo = _uiState.value.healthInfo
        var met = 0f
        if (shangXiaZhiReport != null && healthInfo != null) {
            val cal = shangXiaZhiReport.activeCal
            val activeDuration = shangXiaZhiReport.activeDuration
            val weight = healthInfo.weight
            if (activeDuration > 0 && weight > 0) {
                // mets=卡路里/主动模式运动时间(分钟)/体重(kg)/0.0167
                met = cal / activeDuration / 60 / weight / 0.0167f
            }
        }
        if (met > 0f) {
            _uiState.update {
                it.copy(
                    healthInfo = healthInfo?.copy(met = met),
                )
            }
        }
        viewModelScope.launch {
            _uiState.value.healthInfo?.let {
                orderInfoRepository.insert(OrderInfo(orderId = it.orderId))
                healthInfoRepository.insert(it)
                ReportActivity.start(it.orderId)
            }
        }
    }

    companion object {
        private val TAG = TrainViewModel::class.java.simpleName
    }

}
