package com.psk.shangxiazhi.login

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.like.common.util.mvi.propertyCollector
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.common.customview.ProgressDialog
import com.psk.common.util.showToast
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.data.model.Login
import com.psk.shangxiazhi.databinding.ActivityLoginBinding
import com.psk.shangxiazhi.main.MainActivity
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
            val phone = mBinding.etPhone.text.toString().trim()
            if (phone.isEmpty()) {
                showToast("手机号码不能为空")
                return@setOnClickListener
            }
            val password = mBinding.etPassword.text.toString().trim()
            if (password.isEmpty()) {
                showToast("密码不能为空")
                return@setOnClickListener
            }
            mViewModel.login(phone = phone, password = password, type = 1, progressDialog = mProgressDialog)
        }
        collectUiState()
    }

    private fun collectUiState() {
        mViewModel.uiState.propertyCollector(this) {
            collectDistinctProperty(LoginUiState::login) {
                Login.setCache(it)
                if (it != null) {
                    MainActivity.start()
                    finish()
                }
            }
            collectEventProperty(LoginUiState::toastEvent) {
                showToast(it)
            }
        }
    }

}
