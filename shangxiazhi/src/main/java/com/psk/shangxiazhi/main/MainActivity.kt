package com.psk.shangxiazhi.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.like.common.util.SPUtils
import com.like.common.util.mvi.propertyCollector
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.common.customview.ProgressDialog
import com.psk.common.util.DataHandler
import com.psk.common.util.showToast
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.data.model.Login
import com.psk.shangxiazhi.databinding.ActivityMainBinding
import com.psk.shangxiazhi.setting.SettingActivity
import com.psk.shangxiazhi.util.SP_LOGIN
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
    private val mProgressDialog by lazy {
        ProgressDialog(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.bindGameManagerService(this)
        mBinding.ivAutonomyTraining.setOnClickListener {
            mViewModel.selectSceneAndDeviceAndStartGame(this)
        }
        mBinding.ivMedicalOrderTraining.setOnClickListener {
            val loginJsonString = SPUtils.getInstance().get<String?>(SP_LOGIN, null)
            println(loginJsonString)
            val login = Gson().fromJson<Login?>(loginJsonString, object : TypeToken<Login>() {}.type)
            println(login)
            val patientToken = login.patient_token
            lifecycleScope.launch {
                DataHandler.collectWithProgress(mProgressDialog, block = {
                    mViewModel.getUser(patientToken)
                }, onError = {
                    showToast("获取用户信息失败")
                }) {
                    println(it)
                }
            }
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
