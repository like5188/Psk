package com.psk.shangxiazhi.history

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.like.common.util.mvi.propertyCollector
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.ActivityHistoryBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

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
    private val mViewModel: HistoryViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.ivLeft.setOnClickListener {
            showPreTime()
        }
        mBinding.ivRight.setOnClickListener {
            showNextTime()
        }
        collectUiState()
        mViewModel.getDateList()
    }

    private fun collectUiState() {
        mViewModel.uiState.propertyCollector(this) {
            collectDistinctProperty(HistoryUiState::dateList) {
                mBinding.tvTime.text = it?.lastOrNull() ?: ""
            }
        }
    }

    private fun showPreTime() {
        val cur = mBinding.tvTime.text.toString()
        if (cur.isEmpty()) {
            return
        }
        val dateList = mViewModel.uiState.value.dateList
        if (dateList.isNullOrEmpty()) {
            return
        }
        val index = dateList.indexOf(cur)
        if (index < 0) {
            return
        }
        if (index - 1 >= 0) {
            mBinding.tvTime.text = dateList[index - 1]
        }
    }

    private fun showNextTime() {
        val cur = mBinding.tvTime.text.toString()
        if (cur.isEmpty()) {
            return
        }
        val dateList = mViewModel.uiState.value.dateList
        if (dateList.isNullOrEmpty()) {
            return
        }
        val index = dateList.indexOf(cur)
        if (index < 0) {
            return
        }
        if (index + 1 < dateList.size) {
            mBinding.tvTime.text = dateList[index + 1]
        }
    }

}
