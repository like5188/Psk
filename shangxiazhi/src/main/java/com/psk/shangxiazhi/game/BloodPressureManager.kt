package com.psk.shangxiazhi.game

import android.util.Log
import com.psk.ble.DeviceType
import com.psk.device.data.model.BloodPressure
import com.psk.device.data.source.BloodPressureRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

@OptIn(KoinApiExtension::class)
class BloodPressureManager : BaseDeviceManager<BloodPressure>(), KoinComponent {
    override val repository by inject<BloodPressureRepository> { parametersOf(DeviceType.BloodPressure) }
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