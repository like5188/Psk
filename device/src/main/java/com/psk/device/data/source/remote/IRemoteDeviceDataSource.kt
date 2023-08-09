package com.psk.device.data.source.remote

interface IRemoteDeviceDataSource {
    fun connect(onConnected: () -> Unit, onDisconnected: (() -> Unit)? = null)
}