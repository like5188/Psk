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
        mBinding.tvUuid.text = "1"
        mBinding.btnLogin.setOnClickListener {
            val uuid = mBinding.tvUuid.text.toString().trim()
            val code = mBinding.etCode.text.toString().trim()
            mViewModel.login(uuid, code, progressDialog = mProgressDialog)
        }
        collectUiState()
    }

    private fun collectUiState() {
        mViewModel.uiState.propertyCollector(this) {
            collectDistinctProperty(LoginUiState::isLogin) {
                mViewModel.setLogin(it ?: false)
                if (it == true) {
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
