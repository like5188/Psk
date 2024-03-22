package com.psk.shangxiazhi.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.like.common.util.showToast
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.shangxiazhi.history.HistoryActivity
import com.psk.shangxiazhi.login.LoginScreen
import com.psk.shangxiazhi.login.LoginViewModel
import com.psk.shangxiazhi.setting.SettingActivity
import com.psk.shangxiazhi.theme.AppTheme
import com.psk.shangxiazhi.train.TrainActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * 主界面
 */
class MainActivity : ComponentActivity() {
    companion object {
        fun start() {
            CommonApplication.sInstance.startActivity<MainActivity>()
        }
    }

    private val mainViewModel: MainViewModel by viewModel()
    private val loginViewModel: LoginViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val scope = rememberCoroutineScope()
            val context = LocalContext.current
            val mainUiState = mainViewModel.uiState.collectAsState().value
            val loginUiState = loginViewModel.uiState.collectAsState().value
            mainUiState.toastEvent?.getContentIfNotHandled()?.let {
                showToast(toastEvent = it)
            }
            loginUiState.toastEvent?.getContentIfNotHandled()?.let {
                showToast(toastEvent = it)
            }
            var time by remember {
                mutableStateOf("")
            }
            time = mainUiState.time
            AppTheme {
                if (!mainUiState.isSplash || loginUiState.isLogin == true) {
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
                }
                if (mainUiState.showLoginScreen) {
                    LaunchedEffect(true) {
                        loginViewModel.getSerialNumber(context)
                    }
                    LoginScreen(loginUiState.serialNumber, loginViewModel.code, { loginViewModel.updateCode(it) }) {
                        scope.launch {
                            loginViewModel.login(loginUiState.serialNumber, loginViewModel.code)
                        }
                    }
                }
                LaunchedEffect(true) {
                    // 必须在主线程，否则在平板中没问题，但是在机顶盒中无法执行下面的代码。原因未知。
                    mainViewModel.isLogin(this@MainActivity)
                }
            }
        }
    }

    override fun onBackPressed() {
        FinishApp().execute(this) { finish() }
    }

}
