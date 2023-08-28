package com.psk.device.data.source.local

import kotlinx.coroutines.flow.Flow

interface IDbDataSource<T> {
    fun listenLatest(startTime: Long): Flow<T?>

    suspend fun getByMedicalOrderId(medicalOrderId: Long): List<T>?

    suspend fun save(data: T)
}