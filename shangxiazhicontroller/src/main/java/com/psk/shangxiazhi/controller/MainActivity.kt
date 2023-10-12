package com.psk.shangxiazhi.controller

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.common.util.showToast
import com.psk.device.data.model.DeviceType
import com.psk.shangxiazhi.controller.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
            ScanDeviceDialogFragment.newInstance(DeviceType.ShangXiaZhi).apply {
                onSelected = {
                    mBinding.tvDevice.text = it.name
                    mBinding.tvAddress.text = it.address
                    mBinding.tvConnectState.text = "连接中……"
                    mViewModel.connect(this@MainActivity, it.name, it.address, {
                        mBinding.tvConnectState.text = "已连接"
                        fetch()
                    }) {
                        mBinding.tvConnectState.text = "未连接"
                    }
                }
            }.show(this)
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
    }

    private fun fetch() {
        lifecycleScope.launch {
            mViewModel.fetch().collect {
                withContext(Dispatchers.Main) {
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
