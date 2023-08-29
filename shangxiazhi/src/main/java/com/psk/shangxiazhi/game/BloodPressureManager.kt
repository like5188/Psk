package com.psk.shangxiazhi.game

import android.util.Log
import com.psk.ble.DeviceType
import com.psk.device.DeviceManager
import com.psk.device.data.model.BloodPressure
import com.psk.device.data.source.BloodPressureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

class BloodPressureManager(private val deviceManager: DeviceManager) : BaseDeviceManager<BloodPressure>() {
    override val repository by lazy {
        deviceManager.createRepository<BloodPressureRepository>(DeviceType.BloodPressure)
    }
    var onBloodPressureDataChanged: ((sbp: Int, dbp: Int) -> Unit)? = null

    override suspend fun handleFlow(flow: Flow<BloodPressure>) {
        Log.d(TAG, "startBloodPressureJob")
        flow.distinctUntilChanged().conflate().collect { value ->
            onBloodPressureDataChanged?.invoke(value.sbp, value.dbp)
        }
    }


    companion object {
        private val TAG = BloodPressureManager::class.java.simpleName
    }
}