package com.psk.shangxiazhi.main

import android.app.Activity
import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.common.util.mvi.propertyCollector
import com.like.common.util.startActivity
import com.psk.ble.DeviceType
import com.psk.common.CommonApplication
import com.psk.common.util.showToast
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.ActivityMainBinding
import com.psk.shangxiazhi.device.SelectDeviceDialogFragment
import com.psk.shangxiazhi.history.HistoryActivity
import com.psk.shangxiazhi.login.LoginActivity
import com.psk.shangxiazhi.report.ReportActivity
import com.psk.shangxiazhi.scene.SceneActivity
import com.psk.shangxiazhi.setting.SettingActivity
import com.twsz.twsystempre.TrainScene
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.atomic.AtomicBoolean

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

    // 是否处于闪屏界面（主题中利于属性 windowBackground 达到闪屏界面效果）
    private val isSplash = AtomicBoolean(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.root.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                return if (isSplash.get()) {
                    // 不放行，不会进行绘制。
                    false
                } else {
                    mBinding.root.viewTreeObserver.removeOnPreDrawListener(this)
                    init()
                    // 放行，准备绘制第一帧。
                    true
                }
            }
        })
        lifecycleScope.launch(Dispatchers.IO) {
            if (mViewModel.isLogin(this@MainActivity)) {
                isSplash.set(false)
            } else {
                LoginActivity.start()
                finish()
            }
        }
    }

    private fun init() {
        mViewModel.bindGameManagerService(this@MainActivity)
        mBinding.ivAutonomyTraining.setOnClickListener {
            selectSceneAndDeviceAndStartGame()
        }
        mBinding.ivTrainingRecords.setOnClickListener {
            HistoryActivity.start()
        }
        mBinding.ivSetting.setOnClickListener {
            SettingActivity.start()
        }
        collectUiState()
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
                    mViewModel.uiState.value.gameManagerService?.start(it, scene, resistanceInt = 1, passiveModule = true, timeInt = 1) {
                        ReportActivity.start(reports = it)
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
