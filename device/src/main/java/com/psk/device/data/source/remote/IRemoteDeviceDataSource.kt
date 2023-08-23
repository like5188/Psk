package com.psk.device.data.source.remote

interface IRemoteDeviceDataSource {
    /**
     * 启用该设备
     */
    fun enable(address: String)
}