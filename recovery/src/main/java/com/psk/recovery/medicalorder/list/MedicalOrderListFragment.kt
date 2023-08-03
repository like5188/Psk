package com.psk.recovery.medicalorder.list

import androidx.core.os.bundleOf
import androidx.recyclerview.widget.RecyclerView
import com.like.paging.PagingResult
import com.like.recyclerview.adapter.CombineAdapter
import com.psk.common.customview.RecyclerViewFragment
import com.psk.common.util.DataHandler
import com.psk.common.util.showToast
import com.psk.recovery.data.model.embedded.MedicalOrderAndMonitorDevice
import org.koin.androidx.viewmodel.ext.android.viewModel

class MedicalOrderListFragment : RecyclerViewFragment<MedicalOrderAndMonitorDevice>() {
    companion object {
        private const val KEY_STATUS = "KEY_STATUS"
        fun newInstance(status: Int?): MedicalOrderListFragment {
            return MedicalOrderListFragment().apply {
                arguments = bundleOf(
                    KEY_STATUS to status
                )
            }
        }
    }

    private val mViewModel: MedicalOrderListViewModel by viewModel()

    override fun createCombineAdapter(recyclerView: RecyclerView): CombineAdapter<MedicalOrderAndMonitorDevice> {
        return DataHandler.createPagingAdapter(
            recyclerView = recyclerView,
            itemAdapter = MedicalOrderListAdapter().apply {
                addOnItemClickListener {
                }
            },
            onError = { requestType, throwable ->
                context?.showToast(throwable)
            }
        )
    }

    override fun getPagingResult(): PagingResult<List<MedicalOrderAndMonitorDevice>?> {
        val status = arguments?.getInt(KEY_STATUS) ?: 3
        return mViewModel.getMedicalOrderAndMonitorDevicesResult(status)
    }

}
