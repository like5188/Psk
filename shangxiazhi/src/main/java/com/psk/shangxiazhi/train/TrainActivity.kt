package com.psk.shangxiazhi.train

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.like.common.util.mvi.propertyCollector
import com.like.common.util.startActivity
import com.like.common.util.visible
import com.psk.ble.DeviceType
import com.psk.common.CommonApplication
import com.psk.common.util.showToast
import com.psk.device.data.model.ShangXiaZhiParams
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.data.model.BleScanInfo
import com.psk.shangxiazhi.data.model.IReport
import com.psk.shangxiazhi.data.model.ShangXiaZhiReport
import com.psk.shangxiazhi.databinding.ActivityTrainBinding
import com.psk.shangxiazhi.report.ReportActivity
import com.psk.shangxiazhi.scene.SceneActivity
import com.twsz.twsystempre.TrainScene
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 训练界面
 */
class TrainActivity : AppCompatActivity() {
    companion object {
        fun start() {
            CommonApplication.sInstance.startActivity<TrainActivity>()
        }
    }

    private val mBinding: ActivityTrainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_train)
    }
    private val mViewModel: TrainViewModel by viewModel()
    private var deviceMap: Map<DeviceType, BleScanInfo>? = null
    private var shangXiaZhiParams: ShangXiaZhiParams? = null
    private var minTargetHeartRate: Int = 0
    private var maxTargetHeartRate: Int = 0
    private var scene: TrainScene? = null
    private var age = 0
    private var weight = 0
    private var bloodPressureBefore = 0
    private var bloodPressureAfter = 0
    private var reports: List<IReport>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.bindGameManagerService(this)
        mBinding.llDevice.setOnClickListener {
            SelectDeviceDialogFragment.newInstance(
                arrayOf(
                    DeviceType.ShangXiaZhi,
                    DeviceType.BloodOxygen,
                    DeviceType.BloodPressure,
                    DeviceType.HeartRate,
                )
            ).apply {
                onSelected = {
                    deviceMap = it
                    if (it.containsKey(DeviceType.BloodPressure)) {
                        mBinding.llBloodPressureBefore.visible()
                        mBinding.llBloodPressureAfter.visible()
                    }
                    if (it.containsKey(DeviceType.HeartRate)) {
                        mBinding.llTargetHeartRate.visible()
                    }
                }
            }.show(this)
        }
        mBinding.llScene.setOnClickListener {
            SceneActivity.start(this) {
                if (it.resultCode != Activity.RESULT_OK) {
                    return@start
                }
                scene = it.data?.getSerializableExtra(SceneActivity.KEY_DATA) as? TrainScene
            }
        }
        mBinding.llSetShangXiaZhiParams.setOnClickListener {
            SetShangXiaZhiPramsDialogFragment.newInstance().apply {
                onSelected = {
                    shangXiaZhiParams = it
                }
            }.show(this)
        }
        mBinding.llPersonInfo.setOnClickListener {
            PersonInfoDialogFragment.newInstance().apply {
                onSelected = { age, weight ->
                    this@TrainActivity.age = age
                    this@TrainActivity.weight = weight
                }
            }.show(this)
        }
        mBinding.llTargetHeartRate.setOnClickListener {
            if (age == 0) {
                showToast("请填写基本信息中的年龄")
                return@setOnClickListener
            }
            val bleSanInfo = deviceMap?.get(DeviceType.HeartRate) ?: return@setOnClickListener
            MeasureTargetHeartRateDialogFragment.newInstance(age, bleSanInfo.name, bleSanInfo.address).apply {
                onSelected = { minTargetHeartRate: Int, maxTargetHeartRate: Int ->
                    this@TrainActivity.minTargetHeartRate = minTargetHeartRate
                    this@TrainActivity.maxTargetHeartRate = maxTargetHeartRate
                }
            }.show(this)
        }
        mBinding.btnTrain.setOnClickListener {
            val deviceMap = this.deviceMap
            if (deviceMap.isNullOrEmpty()) {
                showToast("请先选择设备")
                return@setOnClickListener
            }
            if (scene == null) {
                showToast("请选择游戏场景")
                return@setOnClickListener
            }
            if (shangXiaZhiParams == null) {
                showToast("请设置上下肢参数")
                return@setOnClickListener
            }
            if (weight == 0) {
                showToast("请填写基本信息中的体重")
                return@setOnClickListener
            }
            mViewModel.uiState.value.gameManagerService?.start(deviceMap, scene!!, shangXiaZhiParams) {
                reports = it
            }
        }
        mBinding.btnReport.setOnClickListener {
            val deviceMap = this.deviceMap
            if (deviceMap.isNullOrEmpty()) {
                showToast("请先选择设备")
                return@setOnClickListener
            }
            val shangXiaZhiReport = reports?.firstOrNull {
                it is ShangXiaZhiReport
            } as? ShangXiaZhiReport
            if (shangXiaZhiReport == null) {
                showToast("请先开始训练")
                return@setOnClickListener
            }
            val met = ((shangXiaZhiReport.activeCal + shangXiaZhiReport.passiveCal) /
                    (shangXiaZhiReport.activeDuration + shangXiaZhiReport.passiveDuration) /
                    weight /
                    0.0167f).toInt()
            ReportActivity.start(reports, minTargetHeartRate, maxTargetHeartRate, met, bloodPressureBefore, bloodPressureAfter)
        }
        collectUiState()
    }

    private fun collectUiState() {
        mViewModel.uiState.propertyCollector(this) {
            collectDistinctProperty(TrainUiState::gameManagerService) {
                it ?: return@collectDistinctProperty
                it.initBle(this@TrainActivity)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.unbindGameManagerService(this)
    }

}
