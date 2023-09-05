package com.psk.shangxiazhi.util

import com.psk.shangxiazhi.data.source.LoginDataSource
import com.psk.shangxiazhi.data.source.ShangXiaZhiBusinessRepository
import com.psk.shangxiazhi.login.LoginViewModel
import com.psk.shangxiazhi.main.MainViewModel
import com.psk.shangxiazhi.report.ReportViewModel
import com.twsz.twsystempre.GameController
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.text.DecimalFormat

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

    //viewModel
    viewModel {
        LoginViewModel(get())
    }
    viewModel {
        MainViewModel(get())
    }
    viewModel {
        ReportViewModel()
    }

    //GameController
    single {
        GameController(get())
    }
    //DecimalFormat
    single {
        DecimalFormat("######0.00")
    }
}
