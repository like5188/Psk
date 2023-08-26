package com.psk.device.data.source

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface IRepository<T> {
    fun enable(name: String, address: String)

    suspend fun getListByMedicalOrderId(medicalOrderId: Long): List<T>?

    fun getFlow(scope: CoroutineScope, medicalOrderId: Long, interval: Long = 1000): Flow<T>
}