package com.psk.shangxiazhi.main

import android.os.Bundle
import android.view.ViewTreeObserver
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.common.util.mvi.propertyCollector
import com.like.common.util.showToast
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.ActivityMainBinding
import com.psk.shangxiazhi.history.HistoryActivity
import com.psk.shangxiazhi.login.LoginActivity
import com.psk.shangxiazhi.setting.SettingActivity
import com.psk.shangxiazhi.train.TrainActivity
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
        mViewModel.init(this)
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
        lifecycleScope.launch(Dispatchers.Main) {
            // 必须在主线程，否则在平板中没问题，但是在机顶盒中无法执行下面的代码。原因未知。
            if (mViewModel.isLogin(this@MainActivity)) {
                isSplash.set(false)
            } else {
                LoginActivity.start()
                finish()
            }
        }
    }

    private fun init() {
        mBinding.ivAutonomyTraining.setOnClickListener {
            TrainActivity.start()
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
            collectDistinctProperty(MainUiState::time) {
                mBinding.tvTime.text = it
            }
            collectNotHandledEventProperty(MainUiState::toastEvent) {
                showToast(toastEvent = it)
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
            finish()
        }
    }

}
