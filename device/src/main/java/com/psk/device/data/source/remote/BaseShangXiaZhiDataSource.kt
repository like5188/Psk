package com.psk.device.data.source.remote

import com.psk.device.DeviceType
import com.psk.device.data.model.ShangXiaZhi
import kotlinx.coroutines.flow.Flow

abstract class BaseShangXiaZhiDataSource(deviceType: DeviceType) : BaseRemoteDeviceDataSource(deviceType) {

    abstract suspend fun fetch(medicalOrderId: Long): Flow<ShangXiaZhi>

    /**
     * 使上下肢恢复运行
     */
    abstract suspend fun resume()

    /**
     * 使上下肢暂停运行
     */
    abstract suspend fun pause()

    /**
     * 使上下肢停止运行
     */
    abstract suspend fun over()

    /**
     * 设置上下肢参数，设置好后，如果是被动模式，上下肢会自动运行
     * 被动模式下：可设置：时间、速度档位、痉挛等级、智能模式、正反转
     * 主动模式下：可设置：时间、阻力等级、智能模式、正反转
     * @param passiveModule     被动模式
     * @param timeInt           时间 5-30 min
     * @param speedInt          速度 5-60 rpm
     * @param spasmInt          痉挛等级 1-12
     * @param resistanceInt     阻力 1-12
     * @param intelligent       智能模式
     * @param turn2             正转
     */
    abstract suspend fun setParams(
        passiveModule: Boolean, timeInt: Int, speedInt: Int, spasmInt: Int, resistanceInt: Int, intelligent: Boolean, turn2: Boolean
    )

}