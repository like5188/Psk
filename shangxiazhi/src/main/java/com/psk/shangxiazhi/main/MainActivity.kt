package com.psk.shangxiazhi.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.like.common.util.mvi.propertyCollector
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.common.util.showToast
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.ActivityMainBinding
import com.psk.shangxiazhi.setting.SettingActivity
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
            mViewModel.selectSceneAndDeviceAndStartGame(this)
        }
        mBinding.ivMedicalOrderTraining.setOnClickListener {
            showToast("医嘱训练")
        }
        mBinding.ivTrainingRecords.setOnClickListener {
            showToast("训练记录")
        }
        mBinding.ivSetting.setOnClickListener {
            SettingActivity.start()
        }
        collectUiState()
    }

    private fun collectUiState() {
        mViewModel.uiState.propertyCollector(this) {
            collectDistinctProperty(MainUiState::time) {
                mBinding.tvTime.text = it
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.unbindGameManagerService(this)
    }
}
