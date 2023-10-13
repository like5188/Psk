package com.psk.shangxiazhi.controller

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.like.common.util.mvi.propertyCollector
import com.like.common.util.showToast
import com.like.common.util.toIntOrDefault
import com.psk.device.data.model.ShangXiaZhiParams
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
            val shangXiaZhiParams = getShangXiaZhiParams() ?: return@setOnClickListener
            mViewModel.start(shangXiaZhiParams)
        }
        mBinding.btnPause.setOnClickListener {
            mViewModel.pause()
        }
        mBinding.btnStop.setOnClickListener {
            mViewModel.stop()
        }
        mBinding.tvVersion.text = "版本号：${packageManager.getPackageInfo(packageName, 0).versionName}"
        collectUiState()
    }

    private fun getShangXiaZhiParams(): ShangXiaZhiParams? {
        if (mBinding.rgModel.checkedRadioButtonId == -1) {
            showToast("请选择模式")
            return null
        }
        val passiveModel: Boolean = mBinding.rbModel1.isChecked

        if (mBinding.rgIntelligence.checkedRadioButtonId == -1) {
            showToast("请选择智能")
            return null
        }
        val intelligent: Boolean = mBinding.rbIntelligence0.isChecked

        if (mBinding.rgDirection.checkedRadioButtonId == -1) {
            showToast("请选择方向")
            return null
        }
        val forward: Boolean = mBinding.rbDirection0.isChecked

        val time: Int = mBinding.etTime.text.trim().toString().toIntOrDefault(0)
        if (time < 1 || time > 30) {
            showToast("请输入有效时间，范围：1~30分钟")
            return null
        }

        val speedLevel: Int = mBinding.etSpeedLevel.text.trim().toString().toIntOrDefault(0)
        if (speedLevel < 1 || speedLevel > 12) {
            showToast("请输入有效速度挡位，范围：1~12")
            return null
        }

        val spasmLevel: Int = mBinding.etSpasmLevel.text.trim().toString().toIntOrDefault(0)
        if (spasmLevel < 1 || spasmLevel > 12) {
            showToast("请输入有效痉挛等级，范围：1~12")
            return null
        }

        val resistance: Int = mBinding.etResistance.text.trim().toString().toIntOrDefault(0)
        if (resistance < 1 || resistance > 12) {
            showToast("请输入有效阻力等级，范围：1~12")
            return null
        }
        return ShangXiaZhiParams(passiveModel, time, speedLevel, spasmLevel, resistance, intelligent, forward)
    }

    private fun collectUiState() {
        mViewModel.uiState.propertyCollector(this) {
            collectDistinctProperty(MainUiState::name) {
                mBinding.tvDevice.text = it
            }
            collectDistinctProperty(MainUiState::isConnected) {
                mBinding.btnStart.isEnabled = it
                mBinding.btnPause.isEnabled = it
                mBinding.btnStop.isEnabled = it
            }
            collectDistinctProperty(MainUiState::connectState) {
                mBinding.tvConnectState.text = it
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
            mViewModel.stop()
            finish()
        }
    }

}
