package com.psk.shangxiazhi.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.ActivitySplashBinding
import com.psk.shangxiazhi.login.LoginActivity
import com.psk.shangxiazhi.main.MainActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 闪屏界面
 */
class SplashActivity : AppCompatActivity() {
    private val mBinding: ActivitySplashBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_splash)
    }
    private val mViewModel: SplashViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding
        lifecycleScope.launch {
            if (mViewModel.isLogin(this@SplashActivity)) {
                MainActivity.start()
            } else {
                LoginActivity.start()
            }
            finish()
        }
    }

}
