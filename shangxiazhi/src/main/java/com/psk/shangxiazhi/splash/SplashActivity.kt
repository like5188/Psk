package com.psk.shangxiazhi.splash

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.common.util.SPUtils
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.data.model.Login
import com.psk.shangxiazhi.databinding.ActivitySplashBinding
import com.psk.shangxiazhi.login.LoginActivity
import com.psk.shangxiazhi.main.MainActivity
import com.psk.shangxiazhi.util.SP_LOGIN
import kotlinx.coroutines.launch

/**
 * 闪屏界面
 */
class SplashActivity : AppCompatActivity() {
    private val mBinding: ActivitySplashBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_splash)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding
        lifecycleScope.launch {
            if (!SPUtils.getInstance().get<String?>(SP_LOGIN, null).isNullOrEmpty()) {
                MainActivity.start()
            } else {
                LoginActivity.start()
            }
            finish()
        }
    }

}
