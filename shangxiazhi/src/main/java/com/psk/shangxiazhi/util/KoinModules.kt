package com.psk.shangxiazhi.util

import com.psk.shangxiazhi.data.source.HealthInfoRepository
import com.psk.shangxiazhi.data.source.OrderInfoRepository
import com.psk.shangxiazhi.data.source.ShangXiaZhiBusinessRepository
import com.psk.shangxiazhi.data.source.local.LoginDataSource
import com.psk.shangxiazhi.history.HistoryViewModel
import com.psk.shangxiazhi.login.LoginViewModel
import com.psk.shangxiazhi.main.MainViewModel
import com.psk.shangxiazhi.report.ReportViewModel
import com.psk.shangxiazhi.train.TrainViewModel
import com.twsz.twsystempre.GameController
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * File Name: KoinModules.kt
 * Description: koin 依赖注入的 module
 */
val shangXiaZhiModule = module {
    //DataSource
    factory {
        LoginDataSource()
    }

    //Repository
    factory {
        ShangXiaZhiBusinessRepository(get())
    }
    factory {
        HealthInfoRepository()
    }
    factory {
        OrderInfoRepository()
    }

    //viewModel
    viewModel {
        LoginViewModel(get())
    }
    viewModel {
        MainViewModel(get())
    }
    viewModel {
        HistoryViewModel(get())
    }
    viewModel {
        TrainViewModel(get(), get())
    }
    viewModel {
        ReportViewModel(get())
    }

    //GameController
    single {
        GameController(get())
    }
}
