package com.psk.shangxiazhi.history

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.ActivityHistoryBinding

/**
 * 历史界面
 */
class HistoryActivity : AppCompatActivity() {
    companion object {
        fun start() {
            CommonApplication.sInstance.startActivity<HistoryActivity>()
        }
    }

    private val mBinding: ActivityHistoryBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_history)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding
    }

}
