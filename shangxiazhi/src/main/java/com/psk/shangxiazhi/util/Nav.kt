package com.psk.shangxiazhi.util

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.like.common.util.showToast
import com.psk.shangxiazhi.history.HistoryActivity
import com.psk.shangxiazhi.login.LoginScreen
import com.psk.shangxiazhi.login.LoginViewModel
import com.psk.shangxiazhi.LocalNavController
import com.psk.shangxiazhi.main.MainScreen
import com.psk.shangxiazhi.main.MainViewModel
import com.psk.shangxiazhi.setting.SettingActivity
import com.psk.shangxiazhi.train.TrainActivity
import kotlinx.coroutines.launch

/**
 * 导航相关
 */
sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Main : Screen("main_screen")
}

@Composable
fun NavHost(
    mainViewModel: MainViewModel,
    loginViewModel: LoginViewModel
) {
    val navController = LocalNavController.current
    val mainUiState = mainViewModel.uiState.collectAsState().value
    val loginUiState = loginViewModel.uiState.collectAsState().value
    val startDestination = if (!mainUiState.isSplash || loginUiState.isLogin) {
        Screen.Main.route
    } else if (mainUiState.showLoginScreen) {
        Screen.Login.route
    } else {
        ""
    }
    if (startDestination.isNotEmpty()) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            mainGraph(mainViewModel)
            loginGraph(loginViewModel)
        }
    }
    val context = LocalContext.current
    LaunchedEffect(true) {
        // 必须在主线程，否则在平板中没问题，但是在机顶盒中无法执行下面的代码。原因未知。
        mainViewModel.isLogin(context)
    }
}

fun NavGraphBuilder.mainGraph(mainViewModel: MainViewModel) {
    composable(Screen.Main.route) {
        val mainUiState = mainViewModel.uiState.collectAsState().value
        val context = LocalContext.current
        mainUiState.toastEvent?.getContentIfNotHandled()?.let {
            context.showToast(toastEvent = it)
        }
        var time by remember {
            mutableStateOf("")
        }
        time = mainUiState.time
        LaunchedEffect(true) {
            mainViewModel.init(context)
        }
        MainScreen(
            time = time,
            onAutonomyTrainingClick = {
                TrainActivity.start()
            },
            onTrainingRecordsClick = {
                HistoryActivity.start()
            },
            onSettingClick = {
                SettingActivity.start()
            }
        )
        //点击两次返回才关闭app
        BackHandler {
            FinishApp().execute(context)
        }
    }
}

fun NavGraphBuilder.loginGraph(loginViewModel: LoginViewModel) {
    composable(Screen.Login.route) {
        val scope = rememberCoroutineScope()
        val loginUiState = loginViewModel.uiState.collectAsState().value
        val context = LocalContext.current
        loginUiState.toastEvent?.getContentIfNotHandled()?.let {
            context.showToast(toastEvent = it)
        }
        LaunchedEffect(true) {
            loginViewModel.getSerialNumber(context)
        }
        LoginScreen(loginUiState.serialNumber, loginViewModel.code, { loginViewModel.updateCode(it) }) {
            scope.launch {
                loginViewModel.login(loginUiState.serialNumber, loginViewModel.code)
            }
        }
        //点击两次返回才关闭app
        BackHandler {
            FinishApp().execute(context)
        }
    }
}