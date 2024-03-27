package com.psk.shangxiazhi.selectdevice

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.like.common.util.showToast
import com.psk.device.data.model.DeviceType
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.customui.BoxButton
import com.psk.shangxiazhi.customui.Dialog
import com.psk.shangxiazhi.data.model.BleScanInfo

@Preview(widthDp = 1920, heightDp = 1080)
@Composable
private fun SelectDeviceScreenPreview() {
    SelectDeviceScreen(
        showDialog = true,
        deviceTypes = arrayOf(
            DeviceType.ShangXiaZhi,
            DeviceType.BloodOxygen,
            DeviceType.BloodPressure,
            DeviceType.HeartRate,
        ),
        selectedDeviceMap = mutableMapOf(
            DeviceType.ShangXiaZhi to BleScanInfo("上下肢名称", "AA:AA")
        )
    )
}

/**
 * 选择设备对话框界面
 */
@Composable
fun SelectDeviceScreen(
    showDialog: Boolean,
    onDismissRequest: () -> Unit = {},
    deviceTypes: Array<DeviceType>,
    selectedDeviceMap: Map<DeviceType, BleScanInfo>? = null,
    onConfirmClick: (Map<DeviceType, BleScanInfo>) -> Unit = {},
) {
    if (!showDialog) {
        return
    }
    val context = LocalContext.current
    val deviceMap by remember(selectedDeviceMap) {
        mutableStateOf(selectedDeviceMap?.toMutableMap() ?: mutableMapOf())
    }
    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            shadowElevation = 10.dp,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp, vertical = 27.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "选择设备",
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                        Divider()
                    }
                    deviceTypes.forEach {
                        var name by remember {
                            mutableStateOf(deviceMap[it]?.name ?: "")
                        }
                        Item(
                            name = name,
                            des = it.des,
                            onItemClick = {
                                ScanDeviceDialogFragment.newInstance(it).apply {
                                    onSelected = { bleSanInfo ->
                                        deviceMap[it] = bleSanInfo
                                        name = bleSanInfo.name
                                    }
                                }.show(context as FragmentActivity)
                            },
                            onClearClick = {
                                name = ""
                                deviceMap.remove(it)
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(60.dp))
                    Button(
                        modifier = Modifier
                            .width(300.dp)
                            .height(40.dp),
                        onClick = {
                            if (deviceMap.containsKey(DeviceType.ShangXiaZhi)) {
                                onConfirmClick(deviceMap)
                            } else {
                                context.showToast("请先选择上下肢设备")
                            }
                        }
                    ) {
                        Text(text = "确定", fontSize = 16.sp)
                    }
                }
            }

        }
    }
}

@Composable
private fun Item(name: String, des: String, onItemClick: () -> Unit = {}, onClearClick: () -> Unit = {}) {
    Column(modifier = Modifier.clickable {
        onItemClick()
    }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = des,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = name.ifEmpty { "去选择 >" },
                style = MaterialTheme.typography.bodyLarge,
            )
            if (name.isNotEmpty()) {
                Spacer(modifier = Modifier.width(10.dp))
                BoxButton(
                    contentAlignment = Alignment.Center,
                    onClick = onClearClick,
                ) { isPressed, isFocused ->
                    Icon(
                        modifier = Modifier
                            .width(20.dp)
                            .height(20.dp),
                        contentDescription = null,
                        painter = painterResource(id = R.drawable.ic_close),
                        tint = if (isPressed || isFocused) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        Divider()
    }
}
