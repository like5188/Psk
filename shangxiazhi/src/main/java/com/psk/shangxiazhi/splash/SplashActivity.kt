package com.psk.shangxiazhi.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.psk.shangxiazhi.login.LoginActivity
import com.psk.shangxiazhi.main.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 闪屏界面
 */
class SplashActivity : AppCompatActivity() {
    private val mViewModel: SplashViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            delay(1000)
            if (mViewModel.isLogin(this@SplashActivity)) {
                MainActivity.start()
            } else {
                LoginActivity.start()
            }
            finish()
        }
    }

}
