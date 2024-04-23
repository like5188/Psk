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
    private var orderId = 111L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding
        // DevicesFragment 是运动界面
        val devicesFragment = DevicesFragment.newInstance(
            orderId,// 此次运动唯一id。
            // 需要接入的设备信息。BleInfo：说明是蓝牙设备；SocketInfo：说明是socket设备。
            listOf(
//                SocketInfo(DeviceType.HeartRate, "ICV200A", null, 7777),
//                BleInfo(DeviceType.HeartRate, "A00213000316", "A0:02:13:00:03:16"),
                BleInfo(DeviceType.HeartRate, "ER1 0455", "E3:93:39:05:53:94"),
//                BleInfo(DeviceType.BloodOxygen, "O2 0214", "DF:04:89:AA:31:23"),
//                BleInfo(DeviceType.BloodPressure, "BP0282A2210040311", "A4:C1:38:50:10:F1"),
            ),
            "like", "18", "男", "173cm", "75kg"
        )
        // 把 DevicesFragment 界面添加到 Activity 中
        addFragments(R.id.flContainer, 0, devicesFragment)
        // 计时监听：0~480
        devicesFragment.setOnTickListener {
            println(it)
        }
        // 运动完成监听。计时完成并且运动后血压测量完成
        devicesFragment.setOnCompletedListener {
            getHistory()
            finish()
        }
        // 紧急停止点击监听
        devicesFragment.setOnStopListener {
            getHistory()
            finish()
        }
        // 记距设备设置
        lifecycleScope.launch {
            delay(1000)
            devicesFragment.setLapName("记距设备名称")
            devicesFragment.updateLapStatus("未连接")
            delay(3000)
            devicesFragment.updateLapStatus("已连接")
            var lapCount = 0// 圈数
            var lapMeters = 0// 米数
            while (isActive) {
                lapCount++
                lapMeters++
                devicesFragment.updateLapCount(lapCount.toString())// 更新圈数
                devicesFragment.updateLapMeters(lapMeters.toString())// 更新米数
                delay(1000)
            }
        }
    }

    // 获取历史数据
    private fun getHistory() {
        lifecycleScope.launch {
            // 获取血氧数据
            ReportUtils.getInstance(this@SixMinutesActivity).getBloodOxygenListByOrderId(orderId).apply {
                println("bloodOxygenList: $this")
            }
            // 获取心率数据
            ReportUtils.getInstance(this@SixMinutesActivity).getHeartRateListByOrderId(orderId).apply {
                println("heartRateList: $this")
            }
            // 获取运动前血压数据
            ReportUtils.getInstance(this@SixMinutesActivity).getBloodPressureBeforeByOrderId(orderId).apply {
                println("bloodPressureBefore: orderId=$orderId $this")
            }
            // 获取运动后血压数据
            ReportUtils.getInstance(this@SixMinutesActivity).getBloodPressureAfterByOrderId(orderId).apply {
                println("bloodPressureAfter: orderId=$orderId $this")
            }
        }
    }

}