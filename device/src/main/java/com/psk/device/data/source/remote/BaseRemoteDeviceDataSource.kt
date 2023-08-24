package com.psk.device.data.source.remote

import com.psk.device.BleManager
import com.psk.device.Device
import com.psk.device.DeviceType
import com.psk.device.Protocol
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@OptIn(KoinApiExtension::class)
abstract class BaseRemoteDeviceDataSource(
    private val deviceType: DeviceType
) : KoinComponent {
    abstract val protocol: Protocol
    protected val bleManager: BleManager by inject()
    protected lateinit var device: Device
        private set

    /**
     * 启用该设备
     */
    fun enable(address: String) {
        device = Device(address, protocol, deviceType)
        bleManager.addDevices(device)
    }

}