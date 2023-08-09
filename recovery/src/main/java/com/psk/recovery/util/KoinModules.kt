package com.psk.recovery.util

import com.psk.device.BleManager
import com.psk.device.data.db.DeviceDatabaseManager
import com.psk.device.data.db.database.DeviceDatabase
import com.psk.device.data.source.DeviceRepository
import com.psk.device.data.source.db.BloodOxygenDbDataSource
import com.psk.device.data.source.db.BloodPressureDbDataSource
import com.psk.device.data.source.db.HeartRateDbDataSource
import com.psk.device.data.source.db.ShangXiaZhiDbDataSource
import com.psk.device.data.source.remote.IBloodOxygenDataSource
import com.psk.device.data.source.remote.IBloodPressureDataSource
import com.psk.device.data.source.remote.IHeartRateDataSource
import com.psk.device.data.source.remote.IShangXiaZhiDataSource
import com.psk.device.data.source.remote.ble.BP88B180704_BloodPressureDataSource
import com.psk.device.data.source.remote.ble.ER1_HeartRateDataSource
import com.psk.device.data.source.remote.ble.O2_BloodOxygenDataSource
import com.psk.device.data.source.remote.ble.SCI311W_HeartRateDataSource
import com.psk.device.data.source.remote.ble.XZX_ShangXiaZhiDataSource
import com.psk.device.data.source.remote.mock.MockBloodOxygenDataSource
import com.psk.device.data.source.remote.mock.MockBloodPressureDataSource
import com.psk.device.data.source.remote.mock.MockHeartRateDataSource
import com.psk.device.data.source.remote.mock.MockShangXiaZhiDataSource
import com.psk.recovery.data.db.RecoveryDatabaseManager
import com.psk.recovery.data.db.database.RecoveryDatabase
import com.psk.recovery.data.source.RecoveryRepository
import com.psk.recovery.data.source.db.MedicalOrderAndMonitorDevicesDbDataSource
import com.psk.recovery.data.source.db.MedicalOrderDbDataSource
import com.psk.recovery.data.source.db.MonitorDeviceDbDataSource
import com.psk.recovery.medicalorder.add.AddMedicalOrderViewModel
import com.psk.recovery.medicalorder.execute.ExecuteMedicalOrderViewModel
import com.psk.recovery.medicalorder.execute.StartOrPauseManager
import com.psk.recovery.medicalorder.history.HistoryMedicalOrderViewModel
import com.psk.recovery.medicalorder.list.MedicalOrderListViewModel
import com.psk.recovery.shangxiazhi.ShangXiaZhiViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * File Name: KoinModules.kt
 * Description: koin 依赖注入的 module
 * Author: like
 * Date: 2023-06-12
 * Modify:
 * Date:
 */
val recoveryModule = module {
    //DataSource
    factory {
        BloodOxygenDbDataSource(get())
    }
    factory {
        BloodPressureDbDataSource(get())
    }
    factory {
        HeartRateDbDataSource(get())
    }
    factory {
        ShangXiaZhiDbDataSource(get())
    }
    factory {
        MedicalOrderDbDataSource(get())
    }
    factory {
        MonitorDeviceDbDataSource(get())
    }
    factory {
        MedicalOrderAndMonitorDevicesDbDataSource(get())
    }
    factory<IBloodOxygenDataSource>(named("O2")) {
        O2_BloodOxygenDataSource(get())
    }
    factory<IBloodPressureDataSource>(named("BP88B180704")) {
        BP88B180704_BloodPressureDataSource(get())
    }
    factory<IHeartRateDataSource>(named("SCI311W")) {
        SCI311W_HeartRateDataSource(get())
    }
    factory<IHeartRateDataSource>(named("ER1")) {
        ER1_HeartRateDataSource(get())
    }
    factory<IShangXiaZhiDataSource>(named("XZX")) {
        XZX_ShangXiaZhiDataSource(get())
    }
    factory<IBloodOxygenDataSource>(named("mock")) {
        MockBloodOxygenDataSource()
    }
    factory<IBloodPressureDataSource>(named("mock")) {
        MockBloodPressureDataSource()
    }
    factory<IHeartRateDataSource>(named("mock")) {
        MockHeartRateDataSource()
    }
    factory<IShangXiaZhiDataSource>(named("mock")) {
        MockShangXiaZhiDataSource()
    }

    //repository
    factory {
        DeviceRepository(get(), get(), get(), get(), get(named("mock")), get(named("mock")), get(named("mock")), get(named("mock")))
    }
    factory {
        RecoveryRepository(get(), get(), get())
    }

    //viewModel
    viewModel {
        ExecuteMedicalOrderViewModel(get(), get(), get())
    }
    viewModel {
        HistoryMedicalOrderViewModel(get())
    }
    viewModel {
        AddMedicalOrderViewModel(get())
    }
    viewModel {
        MedicalOrderListViewModel(get())
    }
    viewModel {
        ShangXiaZhiViewModel(get())
    }

    //ServerDatabase
    single {
        DeviceDatabaseManager.init(get())
        DeviceDatabaseManager.db
    }
    single {
        RecoveryDatabaseManager.init(get())
        RecoveryDatabaseManager.db
    }

    // Dao
    single {
        get<DeviceDatabase>().bloodOxygenDao()
    }
    single {
        get<DeviceDatabase>().bloodPressureDao()
    }
    single {
        get<DeviceDatabase>().heartRateDao()
    }
    single {
        get<DeviceDatabase>().shangXiaZhiDao()
    }
    single {
        get<RecoveryDatabase>().medicalOrderDao()
    }
    single {
        get<RecoveryDatabase>().monitorDeviceDao()
    }
    factory {
        StartOrPauseManager(get())
    }

    // BleManager
    single {
        BleManager(get())
    }
}
