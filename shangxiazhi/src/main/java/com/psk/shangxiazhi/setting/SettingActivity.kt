package com.psk.shangxiazhi.setting

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.common.util.ApplicationHolder
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.common.customview.ProgressDialog
import com.psk.common.util.DataHandler
import com.psk.common.util.showToast
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.data.model.Login
import com.psk.shangxiazhi.databinding.ActivitySettingBinding
import com.psk.shangxiazhi.login.LoginActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 设置界面
 */
class SettingActivity : AppCompatActivity() {
    companion object {
        fun start() {
            CommonApplication.sInstance.startActivity<SettingActivity>()
        }
    }

    private val mBinding: ActivitySettingBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_setting)
    }
    private val mViewModel: SettingViewModel by viewModel()
    private val mProgressDialog by lazy {
        ProgressDialog(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.tvVersion.text = packageManager.getPackageInfo(packageName, 0).versionName
        mBinding.llVersion.setOnClickListener {
            showToast("当前已经是最新版本！")
        }
        mBinding.llLogout.setOnClickListener {
            val login = Login.getCache() ?: return@setOnClickListener
            lifecycleScope.launch {
                DataHandler.collectWithProgress(mProgressDialog, block = {
                    mViewModel.logout(login.patient_token)
                }, onError = {
                    showToast(it.message ?: "退出登录失败")
                }) {
                    if (it?.code == 0) {
                        showToast("退出登录成功")
                        Login.setCache(null)
                        LoginActivity.start()
                        ApplicationHolder.finishAllActivitiesExclude(LoginActivity::class.java)
                    } else {
                        showToast(it?.msg ?: "退出登录失败")
                    }
                }
            }
        }
    }

}
