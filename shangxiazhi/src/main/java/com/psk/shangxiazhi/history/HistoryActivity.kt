package com.psk.shangxiazhi.history

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.common.util.mvi.propertyCollector
import com.like.common.util.startActivity
import com.like.recyclerview.layoutmanager.WrapLinearLayoutManager
import com.psk.common.CommonApplication
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.databinding.ActivityHistoryBinding
import com.psk.shangxiazhi.report.ReportActivity
import kotlinx.coroutines.launch
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
    private val mItemAdapter by lazy {
        HistoryListAdapter().apply {
            addOnItemClickListener {
                val dateAndData = currentList[it.bindingAdapterPosition]
                val medicalOrderId = dateAndData?.data ?: return@addOnItemClickListener
                lifecycleScope.launch {
                    // todo
//                    ReportActivity.start(mViewModel.getReports(medicalOrderId))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.ivLeft.setOnClickListener {
            mViewModel.getPreTime()
        }
        mBinding.ivRight.setOnClickListener {
            mViewModel.getNextTime()
        }
        mBinding.rv.layoutManager = WrapLinearLayoutManager(this)
        mBinding.rv.adapter = mItemAdapter
        collectUiState()
    }

    private fun collectUiState() {
        mViewModel.uiState.propertyCollector(this) {
            collectDistinctProperty(HistoryUiState::showTime) {
                mBinding.tvTime.text = it ?: ""
            }
            collectDistinctProperty(HistoryUiState::dateAndDataList) {
                mItemAdapter.submitList(it)
            }
        }
    }

}
