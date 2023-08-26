package com.psk.shangxiazhi.main

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.common.util.mvi.propertyCollector
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.common.util.showToast
import com.psk.ble.DeviceType
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.ActivityMainBinding
import com.psk.shangxiazhi.device.SelectDeviceDialogFragment
import com.psk.shangxiazhi.scene.SceneActivity
import com.psk.shangxiazhi.setting.SettingActivity
import com.twsz.twsystempre.TrainScene
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 主界面
 */
class MainActivity : AppCompatActivity() {
    companion object {
        fun start() {
            CommonApplication.sInstance.startActivity<MainActivity>()
        }
    }

    private val mBinding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }
    private val mViewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.bindGameManagerService(this)
        mBinding.ivAutonomyTraining.setOnClickListener {
            selectSceneAndDeviceAndStartGame()
        }
        mBinding.ivMedicalOrderTraining.setOnClickListener {

        }
        mBinding.ivTrainingRecords.setOnClickListener {
            showToast("训练记录")
        }
        mBinding.ivSetting.setOnClickListener {
            SettingActivity.start()
        }
        collectUiState()
        getUser()
    }

    private fun getUser() {
        lifecycleScope.launch {
            mViewModel.getUser(this@MainActivity)
        }
    }

    private fun collectUiState() {
        mViewModel.uiState.propertyCollector(this) {
            collectDistinctProperty(MainUiState::gameManagerService) {
                it ?: return@collectDistinctProperty
                it.initBle(this@MainActivity)
            }
            collectDistinctProperty(MainUiState::time) {
                mBinding.tvTime.text = it
            }
            collectDistinctProperty(MainUiState::userName) {
                mBinding.tvUser.text = "欢迎：$it"
            }
            collectEventProperty(MainUiState::toastEvent) {
                showToast(it)
            }
        }
    }

    /**
     * 选择场景、设备，并启动游戏app
     */
    private fun selectSceneAndDeviceAndStartGame() {
        SceneActivity.start(this) {
            if (it.resultCode != Activity.RESULT_OK) {
                return@start
            }
            val scene = it.data?.getSerializableExtra(SceneActivity.KEY_DATA) as? TrainScene ?: return@start
            SelectDeviceDialogFragment.newInstance(
                arrayOf(
                    DeviceType.ShangXiaZhi,
                    DeviceType.BloodOxygen,
                    DeviceType.BloodPressure,
                    DeviceType.HeartRate,
                )
            ).apply {
                onSelected = {
                    if (!it.containsKey(DeviceType.ShangXiaZhi)) {
                        showToast("请先选择上下肢设备")
                    } else {
                        mViewModel.uiState.value.gameManagerService?.start(it, scene, resistanceInt = 1, passiveModule = true, timeInt = 2)
                    }
                }
                show(this@MainActivity)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.unbindGameManagerService(this)
    }
}
