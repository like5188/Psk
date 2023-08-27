package com.psk.shangxiazhi.game

import android.util.Log
import com.psk.ble.DeviceType
import com.psk.device.data.model.BloodOxygen
import com.psk.device.data.source.BloodOxygenRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

@OptIn(KoinApiExtension::class)
class BloodOxygenManager() : BaseDeviceManager<BloodOxygen>(), KoinComponent {
    override val repository by inject<BloodOxygenRepository> { parametersOf(DeviceType.BloodOxygen) }

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
