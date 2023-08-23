package com.psk.shangxiazhi.main

import android.app.Activity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.common.util.showToast
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.ActivityMainBinding
import com.psk.shangxiazhi.scene.SceneActivity
import com.twsz.twsystempre.TrainScene
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
        mBinding.ivAutonomyTraining.setOnClickListener {
            SceneActivity.start(this) {
                if (it.resultCode != Activity.RESULT_OK) {
                    return@start
                }
                val scene = it.data?.getSerializableExtra(SceneActivity.KEY_DATA) as? TrainScene ?: return@start
                mViewModel.selectDeviceAndStartGame(this, scene)
            }
        }
        mBinding.ivMedicalOrderTraining.setOnClickListener {
            showToast("医嘱训练")
        }
        mBinding.ivTrainingRecords.setOnClickListener {
            showToast("训练记录")
        }
        mViewModel.bindGameManagerService(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mViewModel.unbindGameManagerService(this)
    }
}
