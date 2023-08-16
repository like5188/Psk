package com.psk.shangxiazhi.scene

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.common.util.showToast
import com.psk.device.BleManager
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.ActivitySceneBinding
import com.twsz.twsystempre.TrainScene
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 选择场景界面
 */
class SceneActivity : AppCompatActivity() {
    companion object {
        fun start() {
            CommonApplication.sInstance.startActivity<SceneActivity>()
        }
    }

    private val mBinding: ActivitySceneBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_scene)
    }
    private val bleManager by inject<BleManager>()
    private val mViewModel: SceneViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.iv0.setOnClickListener {
            startGame(TrainScene.country)
        }
        mBinding.iv1.setOnClickListener {
            startGame(TrainScene.dust)
        }
        mBinding.iv2.setOnClickListener {
            startGame(TrainScene.lasa)
        }
        mBinding.iv3.setOnClickListener {
            startGame(TrainScene.sea)
        }
        bleManager.onTip = {
            showToast(it.msg)
        }
        lifecycleScope.launch {
            bleManager.init(this@SceneActivity)
        }
    }

    private fun startGame(scene: TrainScene) {
        mViewModel.start(scene, true, resistanceInt = 1, passiveModule = true, timeInt = 2)
    }

    override fun onDestroy() {
        super.onDestroy()
        bleManager.onDestroy()
        mViewModel.destroy()
    }
}
