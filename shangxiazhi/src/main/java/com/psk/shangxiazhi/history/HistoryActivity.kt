package com.psk.shangxiazhi.history

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.ActivityHistoryBinding
import java.util.Calendar

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
    private val cal: Calendar by lazy {
        Calendar.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.tvTime.text = getTime()
        mBinding.ivLeft.setOnClickListener {
            mBinding.tvTime.text = decrementMonthAndGet()
        }
        mBinding.ivRight.setOnClickListener {
            mBinding.tvTime.text = addMonthAndGet()
        }
    }

    private fun decrementMonthAndGet(): String {
        cal.add(Calendar.MONTH, -1)
        return getTime()
    }

    private fun addMonthAndGet(): String {
        cal.add(Calendar.MONTH, 1)
        return getTime()
    }

    private fun getTime(): String {
        return "${cal.get(Calendar.YEAR)}年${cal.get(Calendar.MONTH) + 1}月"
    }
}
