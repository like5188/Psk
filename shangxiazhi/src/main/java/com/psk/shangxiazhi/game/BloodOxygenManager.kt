package com.psk.shangxiazhi.game

import android.util.Log
import com.psk.ble.DeviceType
import com.psk.device.DeviceManager
import com.psk.device.data.model.BloodOxygen
import com.psk.device.data.source.BloodOxygenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

class BloodOxygenManager(private val deviceManager: DeviceManager) : BaseDeviceManager<BloodOxygen>() {
    override val repository by lazy {
        deviceManager.createRepository<BloodOxygenRepository>(DeviceType.BloodOxygen)
    }

    var onBloodOxygenDataChanged: ((bloodOxygen: Int) -> Unit)? = null

    override suspend fun handleFlow(flow: Flow<BloodOxygen>) {
        Log.d(TAG, "startBloodOxygenJob")
        flow.distinctUntilChanged().conflate().collect { value ->
            onBloodOxygenDataChanged?.invoke(value.value)
        }
    }

    companion object {
        private val TAG = BloodOxygenManager::class.java.simpleName
    }

}
