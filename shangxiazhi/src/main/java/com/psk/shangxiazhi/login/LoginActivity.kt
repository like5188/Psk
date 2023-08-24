package com.psk.shangxiazhi.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.common.customview.ProgressDialog
import com.psk.common.util.DataHandler
import com.psk.common.util.showToast
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.data.model.Login
import com.psk.shangxiazhi.databinding.ActivityLoginBinding
import com.psk.shangxiazhi.main.MainActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 登录界面
 */
class LoginActivity : AppCompatActivity() {
    companion object {
        fun start() {
            CommonApplication.sInstance.startActivity<LoginActivity>()
        }
    }

    private val mBinding: ActivityLoginBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_login)
    }
    private val mViewModel: LoginViewModel by viewModel()
    private val mProgressDialog by lazy {
        ProgressDialog(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.btnLogin.setOnClickListener {
            lifecycleScope.launch {
                val phone = mBinding.etPhone.text.toString().trim()
                if (phone.isEmpty()) {
                    showToast("手机号码不能为空")
                    return@launch
                }
                val password = mBinding.etPassword.text.toString().trim()
                if (password.isEmpty()) {
                    showToast("密码不能为空")
                    return@launch
                }
                DataHandler.collectWithProgress(mProgressDialog, block = {
                    mViewModel.login(
                        phone = phone, password = password, type = 1// 1：病人；2：医生
                    )
                }, onError = {
                    Login.setCache(null)
                }) {
                    if (it?.code == 0) {
                        showToast("登录成功")
                        Login.setCache(it.login)
                        MainActivity.start()
                        finish()
                    } else {
                        Login.setCache(null)
                        showToast(it?.msg ?: "登录失败")
                    }
                }
            }
        }
    }

}
