package com.psk.shangxiazhi.controller

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.like.common.util.mvi.propertyCollector
import com.like.common.util.showToast
import com.psk.shangxiazhi.controller.databinding.ActivityMainBinding

/**
 * 主界面
 */
class MainActivity : AppCompatActivity() {
    private val mBinding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }
    private val mViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.init(this@MainActivity)
        mBinding.btnScan.setOnClickListener {
            mViewModel.scan(this)
        }
        mBinding.btnStart.setOnClickListener {
            mViewModel.resume()
        }
        mBinding.btnPause.setOnClickListener {
            mViewModel.pause()
        }
        mBinding.btnIntelligence.setOnClickListener {
            mViewModel.setIntelligence()
        }
        mBinding.btnForward.setOnClickListener {
            mViewModel.setForward()
        }
        mBinding.btnSpeedLevel.setOnClickListener {
            mViewModel.setSpeedLevel()
        }
        mBinding.btnResistance.setOnClickListener {
            mViewModel.setResistance()
        }
        mBinding.btnModel.setOnClickListener {
            mViewModel.setPassiveModel()
        }
        mBinding.btnSpasmLevel.setOnClickListener {
            mViewModel.setSpasmLevel()
        }
        mBinding.btnTime.setOnClickListener {
            mViewModel.setTime()
        }
        mBinding.tvVersion.text = "版本号：${packageManager.getPackageInfo(packageName, 0).versionName}"
        collectUiState()
    }

    private fun collectUiState() {
        mViewModel.uiState.propertyCollector(this) {
            collectDistinctProperty(MainUiState::name) {
                mBinding.tvDevice.text = it
            }
            collectDistinctProperty(MainUiState::isConnected) {
                mBinding.btnStart.isEnabled = it
                mBinding.btnPause.isEnabled = it
            }
            collectDistinctProperty(MainUiState::connectState) {
                mBinding.tvConnectState.text = it
            }
            collectDistinctProperty(MainUiState::isRunning) {
                mBinding.btnIntelligence.isEnabled = it
                mBinding.btnForward.isEnabled = it
                mBinding.btnSpeedLevel.isEnabled = it
                mBinding.btnResistance.isEnabled = it
                mBinding.btnModel.isEnabled = it
                mBinding.btnSpasmLevel.isEnabled = it
                mBinding.btnTime.isEnabled = it
            }
            collectDistinctProperty(MainUiState::shangXiaZhi) {
                it ?: return@collectDistinctProperty
                mBinding.tvIntelligence.text = when (it.intelligence.toInt()) {
                    0x40 -> "关闭"
                    0x41 -> "打开"
                    else -> ""
                }
                mBinding.tvModel.text = when (it.model.toInt()) {
                    0x01 -> "被动"
                    0x02 -> "主动"
                    else -> ""
                }
                mBinding.tvSpeedLevel.text = it.speedLevel.toString()
                mBinding.tvSpeed.text = it.speed.toString()
                mBinding.tvResistance.text = it.resistance.toString()
                mBinding.tvSpasmLevel.text = it.spasmLevel.toString()
            }
        }
    }

    private var firstTime: Long = 0
    override fun onBackPressed() {
        val secondTime = System.currentTimeMillis()
        if (secondTime - firstTime > 2000) {
            showToast("再按一次退出程序")
            firstTime = secondTime
        } else {
            mViewModel.over()
            finish()
        }
    }

}
