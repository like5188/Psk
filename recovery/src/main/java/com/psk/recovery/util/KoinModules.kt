package com.psk.recovery.util

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
import org.koin.androidx.viewmodel.dsl.viewModel
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
        MedicalOrderDbDataSource(get())
    }
    factory {
        MonitorDeviceDbDataSource(get())
    }
    factory {
        MedicalOrderAndMonitorDevicesDbDataSource(get())
    }

    //repository
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

    //ServerDatabase
    single {
        RecoveryDatabaseManager.init(get())
        RecoveryDatabaseManager.db
    }

    // Dao
    single {
        get<RecoveryDatabase>().medicalOrderDao()
    }
    single {
        get<RecoveryDatabase>().monitorDeviceDao()
    }
    factory {
        StartOrPauseManager(get())
    }

}
