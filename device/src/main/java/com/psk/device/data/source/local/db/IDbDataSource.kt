package com.psk.device.data.source.local.db

import kotlinx.coroutines.flow.Flow

interface IDbDataSource<T> {
    fun listenLatest(startTime: Long): Flow<T?>

    suspend fun getByMedicalOrderId(medicalOrderId: Long): List<T>?

    suspend fun insert(data: T)
}