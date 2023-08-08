package com.psk.recovery.data.source.remote

import com.psk.recovery.data.model.ShangXiaZhi
import kotlinx.coroutines.flow.Flow

interface IShangXiaZhiDataSource : IRemoteDataSource {

    suspend fun fetch(medicalOrderId: Long): Flow<ShangXiaZhi>

}
