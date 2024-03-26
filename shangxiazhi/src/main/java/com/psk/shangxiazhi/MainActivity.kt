package com.psk.shangxiazhi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.psk.shangxiazhi.history.HistoryViewModel
import com.psk.shangxiazhi.login.LoginViewModel
import com.psk.shangxiazhi.main.MainViewModel
import com.psk.shangxiazhi.theme.AppTheme
import com.psk.shangxiazhi.util.NavHost
import org.koin.androidx.viewmodel.ext.android.viewModel

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("no NavController provided!")
}

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModel()
    private val loginViewModel: LoginViewModel by viewModel()
    private val historyViewModel: HistoryViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                CompositionLocalProvider(LocalNavController provides rememberNavController()) {
                    NavHost(
                        mainViewModel = mainViewModel,
                        loginViewModel = loginViewModel,
                        historyViewModel = historyViewModel
                    )
                }
            }
        }
    }

}
