package com.psk.shangxiazhi.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.like.common.util.mvi.propertyCollector
import com.like.common.util.showToast
import com.like.common.util.startActivity
import com.psk.common.CommonApplication
import com.psk.shangxiazhi.history.HistoryActivity
import com.psk.shangxiazhi.login.LoginActivity
import com.psk.shangxiazhi.setting.SettingActivity
import com.psk.shangxiazhi.theme.AppTheme
import com.psk.shangxiazhi.train.TrainActivity
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

    private val mViewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel.init(this)
        setContent {
            AppTheme {
                var time by remember {
                    mutableStateOf("")
                }
                mViewModel.uiState.propertyCollector(this) {
                    collectDistinctProperty(MainUiState::time) {
                        time = it
                    }
                    collectNotHandledEventProperty(MainUiState::toastEvent) {
                        showToast(toastEvent = it)
                    }
                }
                // 是否处于闪屏界面（主题中利于属性 windowBackground 达到闪屏界面效果）
                var isSplash by remember {
                    mutableStateOf(true)
                }
                if (!isSplash) {
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
                LaunchedEffect(true) {
                    // 必须在主线程，否则在平板中没问题，但是在机顶盒中无法执行下面的代码。原因未知。
                    if (mViewModel.isLogin(this@MainActivity)) {
                        isSplash = false
                    } else {
                        LoginActivity.start()
                        finish()
                    }
                }
            }
        }
    }

    private var firstTime: Long = 0
    override fun onBackPressed() {
        val secondTime = System.currentTimeMillis()
        if (secondTime - firstTime > 2000) {
            showToast("再按一次退出程序")
            firstTime = secondTime
        } else {
            finish()
        }
    }

}
