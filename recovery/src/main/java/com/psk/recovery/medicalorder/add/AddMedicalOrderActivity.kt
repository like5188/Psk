package com.psk.recovery.medicalorder.add

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.common.util.mvi.propertyCollector
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.common.util.showToast
import com.psk.recovery.R
import com.psk.recovery.data.model.MedicalOrder
import com.psk.recovery.data.model.MonitorDevice
import com.psk.recovery.databinding.ActivityAddMedicalOrderBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 新增医嘱界面
 */
class AddMedicalOrderActivity : AppCompatActivity() {
    companion object {
        fun start() {
            CommonApplication.sInstance.startActivity<AddMedicalOrderActivity>()
        }
    }

    private val mBinding: ActivityAddMedicalOrderBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_add_medical_order)
    }
    private val mViewModel: AddMedicalOrderViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        collectUiState()
    }

    private fun initView() {
        mBinding.btnPatientConfirm.setOnClickListener {
            lifecycleScope.launch {
                val medicalOrder = MedicalOrder(
                    patientId = 0,
                    planExecuteTime = System.currentTimeMillis() / 1000,
                    planInterval = 10L,
                    status = 0
                )
                val medicalOrderId = mViewModel.addMedicalOrder(medicalOrder)

                val monitorDevices = mutableListOf<MonitorDevice>()
                if (mBinding.viewAddMedicalOrderBloodOxygen.switchBloodOxygen.isChecked) {
                    monitorDevices.add(MonitorDevice(type = 0, medicalOrderId = medicalOrderId))
                }
                if (mBinding.viewAddMedicalOrderBloodPressure.switchBloodPressure.isChecked) {
                    monitorDevices.add(MonitorDevice(type = 1, medicalOrderId = medicalOrderId))
                }
                if (mBinding.viewAddMedicalOrderEcg.switchEcg.isChecked) {
                    monitorDevices.add(MonitorDevice(type = 2, medicalOrderId = medicalOrderId))
                }
                mViewModel.addMonitorDevices(monitorDevices = monitorDevices.toTypedArray())
                finish()
            }
        }
        mBinding.viewAddMedicalOrderBloodOxygen.switchBloodOxygen.setOnCheckedChangeListener { _, _ ->
            updateConfirmButton()
        }
        mBinding.viewAddMedicalOrderBloodPressure.switchBloodPressure.setOnCheckedChangeListener { _, _ ->
            updateConfirmButton()
        }
        mBinding.viewAddMedicalOrderEcg.switchEcg.setOnCheckedChangeListener { _, _ ->
            updateConfirmButton()
        }
        updateConfirmButton()
    }

    private fun updateConfirmButton() {
        mBinding.btnPatientConfirm.isEnabled =
            mBinding.viewAddMedicalOrderBloodOxygen.switchBloodOxygen.isChecked ||
                    mBinding.viewAddMedicalOrderBloodPressure.switchBloodPressure.isChecked ||
                    mBinding.viewAddMedicalOrderEcg.switchEcg.isChecked
    }

    private fun collectUiState() {
        mViewModel.uiState.propertyCollector(this) {
            collectNotHandledEventProperty(AddMedicalOrderUiState::toastEvent) {
                this@AddMedicalOrderActivity.showToast(it)
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}
