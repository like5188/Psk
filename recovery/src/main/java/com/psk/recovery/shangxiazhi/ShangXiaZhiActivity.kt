package com.psk.recovery.shangxiazhi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.common.util.startActivity
import com.like.common.util.toIntOrDefault
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
        mBinding.btnConfirm.setOnClickListener {
            val a0 = mBinding.et0.text.trim().toString().toIntOrDefault(-1)
            val a1 = mBinding.et1.text.trim().toString().toIntOrDefault(-1)
            val a2 = mBinding.et2.text.trim().toString().toIntOrDefault(-1)
            val a3 = mBinding.et3.text.trim().toString().toIntOrDefault(-1)
            val a4 = mBinding.et4.text.trim().toString().toIntOrDefault(-1)
            if (a0 == -1 || a3 == -1 || a4 == -1) {
                return@setOnClickListener
            }
            mViewModel.start(resistanceInt = a0, passiveModule = a3 == 1, timeInt = a4)
        }
        bleManager.onTip = {
            showToast(it.msg)
        }
        lifecycleScope.launch {
            bleManager.init(this@ShangXiaZhiActivity)
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
