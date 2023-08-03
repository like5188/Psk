package com.psk.shangxiazhi.util

import com.psk.shangxiazhi.data.source.LoginRepository
import com.psk.shangxiazhi.data.source.remote.LoginRemoteDataSource
import com.psk.shangxiazhi.login.LoginViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

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
}
