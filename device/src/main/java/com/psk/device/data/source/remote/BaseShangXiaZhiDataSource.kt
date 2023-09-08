package com.psk.device.data.source.remote

import com.psk.ble.DeviceType
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
     *
     * @param passiveModule     被动模式
     * @param time              时间 5-30 min// 被动模式
     * @param speedLevel        速度等级 1-12// 被动模式
     * @param spasmLevel        痉挛等级 1-12// 被动模式
     * @param resistance        阻力 1-12// 主动模式
     * @param intelligent       智能模式
     * @param turn2             正转
     */
    abstract suspend fun setParams(
        passiveModule: Boolean, time: Int, speedLevel: Int, spasmLevel: Int, resistance: Int, intelligent: Boolean, turn2: Boolean
    )

}
