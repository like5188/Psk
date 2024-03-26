package com.psk.shangxiazhi.train

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.psk.device.data.model.DeviceType
import com.psk.shangxiazhi.customui.Bg
import com.psk.shangxiazhi.customui.Title
import com.psk.shangxiazhi.data.model.BleScanInfo

@Preview(widthDp = 1920, heightDp = 1080)
@Composable
private fun TrainScreenPreview() {
    var bloodPressureMeasureType by remember {
        mutableStateOf(0)
    }
    TrainScreen(
        selectedDeviceMap = mapOf(
            DeviceType.ShangXiaZhi to BleScanInfo("上下肢", "AA"),
            DeviceType.HeartRate to BleScanInfo("心电仪", "BB"),
            DeviceType.BloodPressure to BleScanInfo("血压计", "CC"),
            DeviceType.BloodOxygen to BleScanInfo("血氧仪", "DD"),
        ),
        bloodPressureMeasureType = bloodPressureMeasureType,
        onBloodPressureMeasureTypeChanged = {
            bloodPressureMeasureType = it
        }
    )
}

/**
 * 准备训练界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainScreen(
    selectedDeviceMap: Map<DeviceType, BleScanInfo>? = null,
    scene: String = "",
    weight: String = "",
    age: String = "",
    targetHeartRate: String = "",
    bloodPressureBefore: String = "",
    bloodPressureMeasureType: Int = 0,
    onDeviceClick: () -> Unit = {},
    onSceneClick: () -> Unit = {},
    onWeightChanged: (String) -> Unit = {},
    onAgeChanged: (String) -> Unit = {},
    onTargetHeartRateClick: () -> Unit = {},
    onBloodPressureBeforeClick: () -> Unit = {},
    onBloodPressureMeasureTypeChanged: (Int) -> Unit = {},
    onTrainClick: () -> Unit = {},
) {
    val devices by remember {
        val sb = StringBuilder()
        selectedDeviceMap?.forEach {
            val deviceType = it.key
            val deviceName = it.value.name
            if (sb.isNotEmpty()) {
                sb.append("\n")
            }
            sb.append(deviceType.des).append(":").append(deviceName)
        }
        mutableStateOf(sb.toString())
    }
    Bg(contentAlignment = Alignment.Center) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            shadowElevation = 10.dp,
            modifier = Modifier
                .width(1000.dp)
                .fillMaxHeight()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Title("准备训练")
                Row(
                    modifier = Modifier
                        .clickable {
                            onDeviceClick()
                        }
                        .padding(horizontal = 30.dp, vertical = 15.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "*",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Text(
                        text = "设备",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(15.dp))
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = devices.ifEmpty { "去选择 >" },
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.headlineSmall,
                    )
                }
                Divider()
                if (selectedDeviceMap?.containsKey(DeviceType.ShangXiaZhi) == true) {
                    Row(
                        modifier = Modifier
                            .clickable {
                                onSceneClick()
                            }
                            .padding(horizontal = 30.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "*",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Text(
                            text = "游戏场景",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(15.dp))
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = scene.ifEmpty { "去选择 >" },
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                    Divider()
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 30.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "*",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Text(
                            text = "体重(kg)",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(15.dp))
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = weight.ifEmpty { "请填写体重" },
                            onValueChange = onWeightChanged,
                            textStyle = TextStyle(fontSize = 24.sp, textAlign = TextAlign.End)
                        )
                    }
                    Divider()
                }

                if (selectedDeviceMap?.containsKey(DeviceType.HeartRate) == true) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 30.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "年龄",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(15.dp))
                        TextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = age.ifEmpty { "请填写年龄" },
                            onValueChange = onAgeChanged,
                            textStyle = TextStyle(fontSize = 24.sp, textAlign = TextAlign.End)
                        )
                    }
                    Divider()
                    Row(
                        modifier = Modifier
                            .clickable {
                                onTargetHeartRateClick()
                            }
                            .padding(horizontal = 30.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "靶心率",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(15.dp))
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = targetHeartRate.ifEmpty { "去测量 >" },
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                    Divider()
                }

                if (selectedDeviceMap?.containsKey(DeviceType.BloodPressure) == true) {
                    Row(
                        modifier = Modifier
                            .clickable {
                                onBloodPressureBeforeClick()
                            }
                            .padding(horizontal = 30.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "运动前血压",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(15.dp))
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = bloodPressureBefore.ifEmpty { "去测量 >" },
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                    Divider()
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 30.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "*",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.error,
                        )
                        Text(
                            text = "运动中血压测量方式",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Column {
                            Row(
                                modifier = Modifier.clickable {
                                    onBloodPressureMeasureTypeChanged(1)
                                },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = bloodPressureMeasureType == 1, onClick = null)
                                Text(
                                    text = "手动测量(随时手动打开血压仪测量)",
                                    style = MaterialTheme.typography.headlineSmall,
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.clickable {
                                    onBloodPressureMeasureTypeChanged(2)
                                },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = bloodPressureMeasureType == 2, onClick = null)
                                Text(
                                    text = "自动测量(每隔 5 分钟自动测量一次)",
                                    style = MaterialTheme.typography.headlineSmall,
                                )
                            }
                        }
                    }
                    Divider()
                }

                Spacer(modifier = Modifier.height(60.dp))
                Button(
                    modifier = Modifier
                        .width(500.dp)
                        .height(60.dp),
                    onClick = onTrainClick
                ) {
                    Text(text = "开始训练", fontSize = 20.sp)
                }
            }

        }
    }

}
