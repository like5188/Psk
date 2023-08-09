package com.psk.recovery.data.source.remote

import com.psk.recovery.data.model.ShangXiaZhi
import kotlinx.coroutines.flow.Flow

interface IShangXiaZhiDataSource : IRemoteDataSource {

    suspend fun fetch(medicalOrderId: Long): Flow<ShangXiaZhi>

    suspend fun start()

    suspend fun stop()

    suspend fun pause()

    /**
     * @param passiveModule     被动模式
     * @param timeInt           时间 5-30 min
     * @param speedInt          速度 5-60 rpm
     * @param spasmInt          痉挛等级 1-12
     * @param resistanceInt     阻力 1-12
     * @param intelligent       智能模式
     * @param turn2             正转
     */
    suspend fun setParams(
        passiveModule: Boolean,
        timeInt: Int,
        speedInt: Int,
        spasmInt: Int,
        resistanceInt: Int,
        intelligent: Boolean,
        turn2: Boolean
    )
}
