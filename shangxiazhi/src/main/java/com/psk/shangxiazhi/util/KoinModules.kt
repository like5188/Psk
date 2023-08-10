package com.psk.shangxiazhi.util

import com.psk.shangxiazhi.data.source.LoginRepository
import com.psk.shangxiazhi.data.source.remote.LoginRemoteDataSource
import com.psk.shangxiazhi.login.LoginViewModel
import com.psk.shangxiazhi.scene.SceneViewModel
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
        LoginRemoteDataSource()
    }

    //Repository
    factory {
        LoginRepository(get())
    }

    //viewModel
    viewModel {
        LoginViewModel(get())
    }
    viewModel {
        SceneViewModel(get(), get())
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
