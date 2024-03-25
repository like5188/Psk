package com.psk.shangxiazhi.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.psk.shangxiazhi.login.LoginViewModel
import com.psk.shangxiazhi.theme.AppTheme
import org.koin.androidx.viewmodel.ext.android.viewModel

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("no NavController provided!")
}

/**
 * 主界面
 */
class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModel()
    private val loginViewModel: LoginViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                CompositionLocalProvider(LocalNavController provides rememberNavController()) {
                    NavHost(mainViewModel = mainViewModel, loginViewModel = loginViewModel)
                }
            }
        }
    }

}
