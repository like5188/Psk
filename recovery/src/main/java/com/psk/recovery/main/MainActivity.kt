package com.psk.recovery.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.psk.recovery.R
import com.psk.recovery.databinding.ActivityMainBinding
import com.psk.recovery.medicalorder.add.AddMedicalOrderActivity
import com.psk.recovery.medicalorder.list.MedicalOrderListActivity

/**
 * 主界面
 */
class MainActivity : AppCompatActivity() {
    private val mBinding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding.btnAddMedicalOrder.setOnClickListener {
            AddMedicalOrderActivity.start()
        }
        mBinding.btnMedicalOrderList.setOnClickListener {
            MedicalOrderListActivity.start(3)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

}
