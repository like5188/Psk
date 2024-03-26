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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.like.common.util.showToast
import com.psk.device.data.model.DeviceType
import com.psk.shangxiazhi.LocalNavController
import com.psk.shangxiazhi.R
import com.psk.shangxiazhi.customui.BoxButton
import com.psk.shangxiazhi.customui.Title
import com.psk.shangxiazhi.data.model.BleScanInfo

@Preview(widthDp = 1920, heightDp = 1080)
@Composable
private fun SelectDeviceScreenPreview() {
//    Item("上下肢", "aldshfksadjl")
    SelectDeviceScreen(
        deviceTypes = arrayOf(
            DeviceType.ShangXiaZhi,
            DeviceType.BloodOxygen,
            DeviceType.BloodPressure,
            DeviceType.HeartRate,
        )
    )
}

/**
 * 设置界面
 */
@Composable
fun SelectDeviceScreen(
    deviceTypes: Array<DeviceType>,
    selectedDeviceMap: MutableMap<DeviceType, BleScanInfo>? = null,
    onConfirmClick: (Map<DeviceType, BleScanInfo>) -> Unit = {},
) {
    val navController = LocalNavController.current
    val context = LocalContext.current
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
                Title("选择设备")
                deviceTypes.forEach {
                    var name by remember {
                        mutableStateOf(selectedDeviceMap?.get(it)?.name ?: "")
                    }
                    Item(
                        name = name,
                        des = it.des,
                        onItemClick = {
                            ScanDeviceDialogFragment.newInstance(it).apply {
                                onSelected = { bleSanInfo ->
                                    selectedDeviceMap?.set(it, bleSanInfo)
                                    name = bleSanInfo.name
                                }
                            }.show(context as FragmentActivity)
                        },
                        onClearClick = {
                            name = ""
                            selectedDeviceMap?.remove(it)
                        }
                    )
                }
                Spacer(modifier = Modifier.height(100.dp))
                Button(
                    modifier = Modifier
                        .width(500.dp)
                        .height(60.dp),
                    onClick = {
                        if (selectedDeviceMap?.containsKey(DeviceType.ShangXiaZhi) == true) {
                            onConfirmClick(selectedDeviceMap)
                            navController.navigateUp()
                        } else {
                            context.showToast("请先选择上下肢设备")
                        }
                    }
                ) {
                    Text(text = "确定", fontSize = 20.sp)
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
                .padding(horizontal = 30.dp, vertical = 30.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = des,
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = name.ifEmpty { "去选择 >" },
                style = MaterialTheme.typography.headlineSmall,
            )
            if (name.isNotEmpty()) {
                Spacer(modifier = Modifier.width(20.dp))
                BoxButton(
                    contentAlignment = Alignment.Center,
                    onClick = onClearClick,
                ) { isPressed, isFocused ->
                    Icon(
                        modifier = Modifier
                            .width(40.dp)
                            .height(40.dp),
                        contentDescription = null,
                        painter = painterResource(id = R.drawable.ic_close),
                        tint = if (isPressed || isFocused) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
        Divider()
    }
}
