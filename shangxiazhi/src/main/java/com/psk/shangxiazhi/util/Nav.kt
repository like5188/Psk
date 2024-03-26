package com.psk.shangxiazhi.util

import android.view.KeyEvent
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.like.ble.central.util.PermissionUtils
import com.like.common.util.showToast
import com.psk.device.data.model.DeviceType
import com.psk.shangxiazhi.LocalNavController
import com.psk.shangxiazhi.history.HistoryScreen
import com.psk.shangxiazhi.history.HistoryViewModel
import com.psk.shangxiazhi.login.LoginScreen
import com.psk.shangxiazhi.login.LoginViewModel
import com.psk.shangxiazhi.main.MainScreen
import com.psk.shangxiazhi.main.MainViewModel
import com.psk.shangxiazhi.report.ReportActivity
import com.psk.shangxiazhi.selectdevice.SelectDeviceScreen
import com.psk.shangxiazhi.setting.SettingScreen
import com.psk.shangxiazhi.train.TrainScreen
import com.psk.shangxiazhi.train.TrainViewModel
import kotlinx.coroutines.launch

/**
 * 导航相关
 */
sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object Main : Screen("main_screen")
    object Setting : Screen("setting_screen")
    object History : Screen("history_screen")
    object Train : Screen("train_screen")
    object SelectDevice : Screen("select_device_screen")
}

@Composable
fun NavHost(
    mainViewModel: MainViewModel,
    loginViewModel: LoginViewModel,
    historyViewModel: HistoryViewModel,
    trainViewModel: TrainViewModel
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
            settingGraph()
            historyGraph(historyViewModel)
            trainGraph(trainViewModel)
            selectDeviceGraph(trainViewModel)
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
        val navController = LocalNavController.current
        val mainUiState = mainViewModel.uiState.collectAsState().value
        val context = LocalContext.current
        mainUiState.toastEvent?.getContentIfNotHandled()?.let {
            context.showToast(toastEvent = it)
        }
        LaunchedEffect(true) {
            mainViewModel.init(context)
        }
        MainScreen(
            time = mainUiState.time,
            onAutonomyTrainingClick = {
                navController.navigate(Screen.Train.route)
            },
            onTrainingRecordsClick = {
                navController.navigate(Screen.History.route)
            },
            onSettingClick = {
                navController.navigate(Screen.Setting.route)
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

fun NavGraphBuilder.settingGraph() {
    composable(Screen.Setting.route) {
        val context = LocalContext.current
        val navController = LocalNavController.current
        val version by remember {
            mutableStateOf(context.packageManager.getPackageInfo(context.packageName, 0).versionName)
        }
        SettingScreen(version)
        BackHandler {
            navController.navigateUp()
        }
    }
}

fun NavGraphBuilder.historyGraph(historyViewModel: HistoryViewModel) {
    composable(Screen.History.route) {
        val historyUiState = historyViewModel.uiState.collectAsState().value
        val navController = LocalNavController.current
        LaunchedEffect(true) {
            historyViewModel.getHistory()
        }
        HistoryScreen(showTime = historyUiState.showTime, orderInfoList = historyUiState.orderInfoList,
            onPreClick = {
                historyViewModel.getPreTime()
            },
            onNextClick = {
                historyViewModel.getNextTime()
            },
            onItemClick = {
                ReportActivity.start(it.orderId)
            }
        )
        BackHandler {
            navController.navigateUp()
        }
    }
}

fun NavGraphBuilder.trainGraph(trainViewModel: TrainViewModel) {
    composable(Screen.Train.route) {
        val scope = rememberCoroutineScope()
        val trainUiState = trainViewModel.uiState.collectAsState().value
        val navController = LocalNavController.current
        val context = LocalContext.current
        trainUiState.toastEvent?.getContentIfNotHandled()?.let {
            context.showToast(toastEvent = it)
        }
        DisposableEffect(true) {
            trainViewModel.bindGameManagerService(context)
            onDispose {
                trainViewModel.unbindGameManagerService(context)
            }
        }
        var bloodPressureMeasureType by remember {
            mutableStateOf(0)
        }
        TrainScreen(
            selectedDeviceMap = trainUiState.deviceMap,
            scene = trainUiState.scene?.des ?: "",
            weight = if (trainUiState.healthInfo == null || trainUiState.healthInfo.weight == 0) {
                ""
            } else {
                trainUiState.healthInfo.weight.toString()
            },
            age = if (trainUiState.healthInfo == null || trainUiState.healthInfo.age == 0) {
                ""
            } else {
                trainUiState.healthInfo.age.toString()
            },
            targetHeartRate = if (trainUiState.healthInfo == null || trainUiState.healthInfo.minTargetHeartRate == 0 || trainUiState.healthInfo.maxTargetHeartRate == 0) {
                ""
            } else {
                "${trainUiState.healthInfo.minTargetHeartRate}~${trainUiState.healthInfo.maxTargetHeartRate}"
            },
            bloodPressureBefore = trainUiState.healthInfo?.bloodPressureBefore?.toString() ?: "",
            bloodPressureMeasureType = bloodPressureMeasureType,
            onDeviceClick = {
                scope.launch {
                    if (PermissionUtils.requestScanEnvironment(context as FragmentActivity)) {
                        navController.navigate(Screen.SelectDevice.route)
                    } else {
                        context.showToast("无法选择设备！缺少扫描蓝牙设备需要的权限：位置信息、查找连接附近设备")
                    }
                }
            },
            onSceneClick = {
                trainViewModel.selectTrainScene(context as FragmentActivity)
            },
            onWeightChanged = {
                trainViewModel.setWeight(it.toIntOrNull() ?: 0)
            },
            onAgeChanged = {
                trainViewModel.setAge(it.toIntOrNull() ?: 0)
            },
            onTargetHeartRateClick = {
                trainViewModel.measureTargetHeart(context as FragmentActivity)
            },
            onBloodPressureBeforeClick = {
                trainViewModel.measureBloodPressureBefore(context as FragmentActivity)
            },
            onBloodPressureMeasureTypeChanged = {
                bloodPressureMeasureType = it
            },
            onTrainClick = {
                trainViewModel.train(bloodPressureMeasureType)
            }
        )
        BackHandler {
            navController.navigateUp()
        }
        if (trainUiState.isTrainCompleted) {
            // 训练完成
            // 如果没有血压仪
            if (trainUiState.deviceMap?.containsKey(DeviceType.BloodPressure) != true) {
                trainViewModel.report()
                navController.navigateUp()
                return@composable
            }
            // 如果有血压仪
            val dialog =
                AlertDialog.Builder(context).setMessage("是否进行运动后血压测试?").setNegativeButton("取消") { _, _ ->
                    trainViewModel.report()
                    navController.navigateUp()
                }.setPositiveButton("去测量") { _, _ ->
                    trainViewModel.measureBloodPressureAfter(context as FragmentActivity) {
                        trainViewModel.report()
                        navController.navigateUp()
                    }
                }.create()
            dialog.setOnKeyListener { dialog, keyCode, event ->
                // 返回键点击事件处理
                if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    trainViewModel.report()
                    navController.navigateUp()
                }
                false
            }
            dialog.show()
        }
    }
}

fun NavGraphBuilder.selectDeviceGraph(trainViewModel: TrainViewModel) {
    composable(Screen.SelectDevice.route) {
        val navController = LocalNavController.current
        val deviceTypes by remember {
            mutableStateOf(
                arrayOf(
                    DeviceType.ShangXiaZhi,
                    DeviceType.BloodOxygen,
                    DeviceType.BloodPressure,
                    DeviceType.HeartRate,
                )
            )
        }
        SelectDeviceScreen(
            deviceTypes = deviceTypes,
            selectedDeviceMap = trainViewModel.uiState.collectAsState().value.deviceMap?.toMutableMap()
        ) {
            trainViewModel.selectDevices(it)
        }
        BackHandler {
            navController.navigateUp()
        }
    }
}
