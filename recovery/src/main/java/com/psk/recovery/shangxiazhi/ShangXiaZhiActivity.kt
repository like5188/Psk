package com.psk.recovery.shangxiazhi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.common.util.showToast
import com.psk.device.BleManager
import com.psk.recovery.R
import com.psk.recovery.databinding.ActivityShangXiaZhiBinding
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 上下肢对接界面
 */
class ShangXiaZhiActivity : AppCompatActivity() {
    companion object {
        fun start() {
            CommonApplication.sInstance.startActivity<ShangXiaZhiActivity>()
        }
    }

    private val mBinding: ActivityShangXiaZhiBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_shang_xia_zhi)
    }
    private val bleManager by inject<BleManager>()
    private val mViewModel: ShangXiaZhiViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding
        lifecycleScope.launch {
            bleManager.onTip = {
                showToast(it.msg)
            }
            bleManager.init(this@ShangXiaZhiActivity)
            mViewModel.start()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        bleManager.onDestroy()
    }

}
