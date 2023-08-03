package com.psk.recovery.data.source.remote

interface IRemoteDataSource {
    fun connect(onConnected: () -> Unit, onDisconnected: (() -> Unit)? = null)
}