package com.psk.app.sixminutes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.like.common.base.addFragments
import com.psk.app.R
import com.psk.app.databinding.ActivitySixMinutesBinding
import com.psk.device.data.model.DeviceType
import com.psk.sixminutes.DevicesFragment
import com.psk.sixminutes.ReportUtils
import com.psk.sixminutes.data.model.BleInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class SixMinutesActivity : AppCompatActivity() {
    private val mBinding: ActivitySixMinutesBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_six_minutes)
    }
    private var orderId = 3L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding
        val devicesFragment = DevicesFragment.newInstance(
            orderId,
            listOf(
//                SocketInfo(DeviceType.HeartRate, "ICV200A", null, 7777),
//                BleInfo(DeviceType.HeartRate, "ER1 0455", "E3:93:39:05:53:94"),
//                BleInfo(DeviceType.BloodOxygen, "O2 0214", "DF:04:89:AA:31:23"),
                BleInfo(DeviceType.BloodPressure, "BP0282A2210040311", "A4:C1:38:50:10:F1"),
            ),
            "like", "18", "男", "173cm", "75kg"
        )
        addFragments(R.id.flContainer, 0, devicesFragment)
        devicesFragment.setOnTickListener {
            println(it)
        }
        devicesFragment.setOnCompletedListener {
            report()
        }
        devicesFragment.setOnStopListener {
            report()
        }
        lifecycleScope.launch {
            delay(1000)
            devicesFragment.setLapName("xxxxxxxxx")
            devicesFragment.updateLapStatus("未连接")
            delay(3000)
            devicesFragment.updateLapStatus("已连接")
            var lapCount = 0
            var lapMeters = 0
            while (isActive) {
                lapCount++
                lapMeters++
                devicesFragment.updateLapCount(lapCount.toString())
                devicesFragment.updateLapMeters(lapMeters.toString())
                delay(1000)
            }
        }
    }

    fun report() {
        lifecycleScope.launch {
            ReportUtils.getInstance().getBloodOxygenListByOrderId(orderId).apply {
                println("bloodOxygenList: $this")
            }
            ReportUtils.getInstance().getHeartRateListByOrderId(orderId).apply {
                println("heartRateList: $this")
            }
            ReportUtils.getInstance().getBloodPressureBeforeByOrderId(orderId).apply {
                println("bloodPressureBefore: $this")
            }
            ReportUtils.getInstance().getBloodPressureAfterByOrderId(orderId).apply {
                println("bloodPressureAfter: $this")
            }
        }
    }

}