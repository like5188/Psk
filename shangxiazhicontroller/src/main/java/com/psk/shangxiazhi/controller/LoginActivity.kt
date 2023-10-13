package com.psk.shangxiazhi.controller

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.like.common.util.showToast
import com.psk.shangxiazhi.controller.databinding.ActivityLoginBinding

/**
 * 登录界面
 */
class LoginActivity : AppCompatActivity() {
    companion object {
        private val accounts = mapOf(
            "13300000000" to "123456",
            "13300000001" to "rkakxc",
            "13300000002" to "ncjmnz",
            "13300000003" to "adercz",
        )
    }

    private val mBinding: ActivityLoginBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_login)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.btnLogin.setOnClickListener {
            val serialNumber = mBinding.etSerialNumber.text.toString().trim()
            val code = mBinding.etCode.text.toString().trim()
            if (accounts.containsKey(serialNumber) && accounts[serialNumber] == code) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                showToast("登录失败，请联系厂家获取有效的账号密码！")
            }
        }
    }

}
