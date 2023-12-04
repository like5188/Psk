package com.psk.device.data.source.remote

import com.like.common.util.Logger
import com.psk.device.data.model.HeartRate
import com.psk.device.data.model.Protocol
import com.psk.device.data.source.remote.base.BaseHeartRateDataSource
import com.starcaretech.stardata.StarData
import com.starcaretech.stardata.common.DataReceiverSample
import com.starcaretech.stardata.data.AlertSwitch
import com.starcaretech.stardata.data.DataPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import java.math.BigDecimal

/*
初始化
StarData.init();
设置数据监听
StarData.setDataReceiver(new DataReceiver() {
            @Override
            public void onDeviceException(DeviceException e) {
                // 设备异常
            }

            @Override
            public void onECGData(byte[] bytes) {
                // 解析后的心电数据（byte）
            }

            @Override
            public void onRetransmissionEcgData(byte[] bytes) {
                // 重传的心电数据（byte）
            }

            @Override
            public void onDataPoints(List<DataPoint> list) {
                // 心电数据点 画波形使用这个就可以
            }

            @Override
            public void onDeviceSettings(DeviceSettings deviceSettings) {
                // 设备设置
            }

            @Override
            public void onDeviceStatus(DeviceStatus deviceStatus) {
                // 设备状态
            }

            @Override
            public void onAlertSwitch(AlertSwitch alertSwitch) {
                // 设备报警开关
            }

            @Override
            public void onSoftVersion(SoftVersion softVersion) {
                // 设备内嵌软件版本
            }

            @Override
            public void onHostInfo(HostInfo hostInfo) {
                // 底座信息
            }

            @Override
            public void onProgress(int i) {
                // 如果底座在上传数据（进度）
            }

            @Override
            public void onTime(long l) {
                // 设备时间
            }

            @Override
            public void onUpgradeStatus(UpgradeStatus upgradeStatus) {
                // 设备升级时状态
            }

            @Override
            public void onCommandResponse(CommandResponse response) {
                // 命令回复
            }
        });
备注：传入的数据解析完后会触发此监听
数据解析
所有设备端发送过来的数据都放入SDK, SDK会自动解析成各种类型的数据（eg：DeviceSettings、DeviceStatus等），触发数据监听DataReceiver

byte[] data = ; // 蓝牙接收到的数据
StarData.putData(data); // 设备通过蓝牙发过来的数据一律通过此方法直接放入
命令组装
通过CommandUtil获取向设备发送的命令

eg: 获取设备状态

CommandUtil.getDeviceStatus()
-备注：通过CommandUtil获取相应的命令，通过蓝牙发送给设备

类属性方法详情
CommandUtil 命令获取工具类
/**
 * Command assembly
 */
public class CommandUtil {

    /**
     * Get specific ID Bluetooth data packet
     * @param packetId Packet id
     * @return Return the command packet sent to the device
     */
    public static byte[] getSpecificECGPacket(int packetId){}

    /**
     * Get device status
     * @return Return the command packet sent to the device
     */
    public static byte[] getDeviceStatus(){}

    /**
     * Get device time
     * @return Return the command packet sent to the device
     */
    public static byte[] getDeviceTime(){}

    /**
     * Set device time
     * @return Return the command packet sent to the device
     */
    public static byte[] setDeviceTime(){}

    /**
     * Get alarm status
     * @return Return the command packet sent to the device
     */
    public static byte[] getAlarmStatus(){}

    /**
     * Set the alarm switch
     * @param isVibration Vibration switch
     * @param isFlicker Light switch
     * @return Return the command packet sent to the device
     */
    public static byte[] setAlarmSwitch(boolean isVibration, boolean isFlicker){}

    /**
     * Set the alarm switch
     * @param alertSwitch AlertSwitch
     * @return Return the command packet sent to the device
     */
    public static byte[] setAlarmSwitch(AlertSwitch alertSwitch){}

    /**
     * Get device settings
     * @return Return the command packet sent to the device
     */
    public static byte[] getDeviceSettings(){}

    /**
     * Set device settings
     * @return Return the command packet sent to the device
     */
    public static byte[] setDeviceSettings(DeviceSettings settings){}

    /**
     * Get device function information
     * @return Return the command packet sent to the device
     */
    public static byte[] getDeviceFunction(){}

    /**
     * Get device software version
     * @return Return the command packet sent to the device
     */
    public static byte[] getDeviceSoftwareVersion(){}

    /**
     * Wipe device cache
     * @return Return the command packet sent to the device
     */
    public static byte[] wipeDeviceCache(){}

    /**
     * Restart device
     * @return Return the command packet sent to the device
     */
    public static byte[] restartDevice(){}

    /**
     * Get base device information
     * @return Return the command packet sent to the device
     */
    public static byte[] getBaseDeviceInfo(){}

    /**
     * Configure the network for the base device
     *
     * @param wifiName Wifi name
     * @param wifiPwd Wifi password
     * @param hotName Hotspot name
     * @param hotPwd Hotspot password
     * @param phoneIp Phone ip
     * @param userId User id
     * @param serverIp Server ip
     * @param isConnectPhone 0x01 Connect phone； 0x00 Connect server
     * @param isConfigNetwork 0x01 Reconfigure； 0x00 Not configured
     * @return Return the command packet sent to the device
     */
    public static byte[] configureBaseDeviceNetwork(String wifiName, String wifiPwd, String hotName, String hotPwd, String phoneIp, String userId, String serverIp, byte isConnectPhone, byte isConfigNetwork){}

    /**
     * Get device factory information
     * @return Return the command packet sent to the device
     */
    public static byte[] getDeviceFactoryInfo(){}

    /**
     * Get the progress of uploading data from the base device
     * @return Return the command packet sent to the device
     */
    public static byte[] getBaseDeviceUploadProgress() {}

    /**
     * Self-check status
     * @return Return the command packet sent to the device
     */
    public static byte[] selfCheckStatus(){}

    /**
     * Shutdown
     * @return Return the command packet sent to the device
     */
    public static byte[] shutdown(){}

    /**
     * Get user information
     * @return Return the command packet sent to the device
     */
    public static byte[] getUserInfo(){}

    /**
     * Get user contact information
     * @return Return the command packet sent to the device
     */
    public static byte[] getUserContactInfo(){}

    /**
     * Get user hospitalization information
     * @return Return the command packet sent to the device
     */
    public static byte[] getUserHospitalizationInfo(){}

    /**
     * Get device setting sampling duration
     * @return Return the command packet sent to the device
     */
    public static byte[] getSamplingDuration(){}

    /**
     * Get start recording time
     * @return Return the command packet sent to the device
     */
    public static byte[] getStartRecordingTime(){}

    /**
     * Get medication status
     * @return Return the command packet sent to the device
     */
    public static byte[] getMedicationStatus(){}

    /**
     * Get notes
     * @return Return the command packet sent to the device
     */
    public static byte[] getNotes(){}

    /**
     * Get ECG data packet reply
     * @return Return the command packet sent to the device
     */
    public static byte[] getECGPacketReply(int packageId){}

    /**
     * Get retransmitted ECG packet reply
     * @return Return the command packet sent to the device
     */
    public static byte[] getRetranECGPacketReply(int packageId){}

    /**
     * Start the upgrade. (Mst or Ble)
     * @param command Mst（0xB0）; Ble（0xB5）
     * @param length Upgrade file length
     * @return Return the command packet sent to the device
     */
    public static byte[] startUpgrade(byte command, int length){}

    /**
     * Send upgrade content
     * @param command Mst（0xB0）; Ble（0xB5）
     * @param packageId package id
     * @param content content
     * @return Return the command packet sent to the device
     */
    public static byte[] sendUpgradeContent(byte command, short packageId, byte[] content){}

    /**
     * Set user id.
     */
    public static byte[] setUserId(String userId) {}
}
DeviceSettings 设备设置
public class DeviceSettings {

    public static final int PACEMARK_ON = 1; // 起搏检测开
    public static final int POWER_FREQUENCY_NOTCH_OFF = 0; // 工频陷波关
    public static final int POWER_FREQUENCY_NOTCH_50HZ = 1; // 50Hz工频陷波关
    public static final int POWER_FREQUENCY_NOTCH_60HZ = 2; // 60Hz工频陷波关
    public static final int HD_DISPLAY_ON = 1; // 高清显示开
    public static final int HD_DISPLAY_OFF = 0; // 高清显示关

    /*
     * 通道数
     */
    private int channels;

    /*
     * 起搏检测
     * 0 无
     * 1 有
     */
    private int pacemark;

    /*
     * 呼吸检测
     * 0 无
     * 1 1路呼吸检测
     * 2 2路呼吸检测
     */
    private int breath;

    /*
     * 工频陷波
     * 0 关闭
     * 1 50Hz
     * 2 60Hz
     */
    private int powerFrequencyNotch;

    /*
     * 高清显示
     * 0 no
     * 1 yes
     */
    private int hdDisplay;

    /*
     * 蓝牙数据传输模式
     * 0 正常模式，所有蓝牙数据都需要传输
     * 1 按需模式，只传输用户拍打标记
     */
    private int transmissionMode;

    /*
     * 低通滤波 PC端会用到，设置时原样回设
     */
    private int lowerFilter;

    /*
     * 波形缩放是否打开
     *  0 关闭
     *  1 打开
     */
    private int zoom;

    // 省略get、set
}
DeviceStatus 设备状态
/*
 * 设备状态
 */
public class DeviceStatus {

    public static final int MEMORY_NORMAL = 0; // 存储正常
    public static final int MEMORY_FULL = 1; // 存储满
    public static final int MEMORY_EXCEPTION = 2; // 存储异常
    public static final int LEAD_OFF = 0; // 导联脱落
    public static final int LOW_POWER = 0; // 低电量
    public static final int HOST_NOT_CONNECT = 0; // 没连接底座
    public static final int HOST_HIGH_VERSION = 1; // 高配版底座
    public static final int HOST_LOW_VERSION = 2; // 低配版底座
    public static final int HOST_CHECKING = 3; // 底座检测中

    /*
     * 存储器状态
     * 0 正常
     * 1 存储满
     * 2 异常
     */
    private int memoryStatus;

    /*
     * 存储总时间
     */
    private int totalStorageTime;

    /*
     * 剩余存储时间
     */
    private int remainingStorageTime;

    /*
     * 蓝牙连接状态
     * 0 断开
     * 1 连接
     */
    private int bleConnectStatus;

    /*
     * 电量
     * 0 低电量
     * 1 1格
     * 2 2格
     * 3 3格
     * 4 4格
     * 6 充电中
     * 7 充电完成
     */
    private int power;

    /*
     * 底座
     * 0 没连接底座
     * 1 高配版
     * 2 低配版
     * 3 检测中
     */
    private int host;

    /*
     * 设备当前状态
     * 0 关机
     * 1 初始开机状态
     * 2 正常工作
     */
    private int status;

    /*
     * 导联状态
     * 0 断开
     * 1 连接
     */
    private int lead;

    // 省略get、set
}
AlertSwitch 设备报警开关状态
/*
 * 报警开关状态
 */
public class AlertSwitch {

    /*
     * 低电量
     * 3 灯、震动开
     * 2 灯开，震动关
     * 1 灯关，震动开
     * 0 灯、震动关
     */
    private int lowPower;

    /*
     * flash
     * 3 灯、震动开
     * 2 灯开，震动关
     * 1 灯关，震动开
     * 0 灯、震动关
     */
    private int flash;

    /*
     * 导联脱落
     * 3 灯、震动开
     * 2 灯开，震动关
     * 1 灯关，震动开
     * 0 灯、震动关
     */
    private int leadOff;

    /*
     * 蓝牙状态
     * 1 灯开
     * 0 灯关
     */
    private int bleStatus;

    // 省略get、set
}
DeviceException 设备是否有异常
/*
 * 设备异常
 */
public class DeviceException {

    /*
     * 重力传感器
     * 0 正常
     * 1 异常
     */
    private int gesensor;

    /*
     * 存储
     * 0 正常
     * 1 异常
     */
    private int flash;

    /*
     * 时钟
     * 0 正常
     * 1 异常
     */
    private int rtc;

    // 省略get、set
}
DataPoint 数据点（采样率为125，1秒钟有125个点）
public class DataPoint {

    /*
     * 标红点
     * 检测到拍打点时会把前后30个点该属性置为true
     */
    private boolean markRed = false;

    /*
     * 数据值
     * 绘制波形时用此值 200=1mv
     */
    private int value;

    /*
     * 起搏
     * 该点是否检测到Pacemark
     */
    private boolean pace = false;

    /*
     * qrs
     * 是否检测到Qrs点
     */
    private boolean dot = false;

    /*
     * qrs list（里面的每个数值都是一个qrs点，值是距离当前点的偏移量）
     */
    private List<Integer> dotXList;

    /*
     * 心率值
     */
    private int heartRate;

    /*
     * 原始值
     * Don`t care
     */
    private int originalValue;

    // 省略get、set
}
HostInfo 底座信息
public class HostInfo {

    /*
     * 错误码
     */
    private int errorCode;

    /*
     * 错误信息描述
     */
    private String errorMsg;

    /*
     * SN编号
     */
    private String sn;

    /*
     * 版本号
     */
    private String version;

    /*
     * 网络状态
     * -1 未知状态
     * 0  空闲
     * 1  正在连接到手机热点
     * 2  用户信息无效
     * 3  Patch Sn 无效
     * 4  正在连接到服务
     * 5  已连接PC端
     * 20 连接网络失败
     * 21 尝试重连网络
     * 22 网络连接成功
     * 23 正在传输数据
     * 25 正在升级
     * 28 传输数据结束
     * 29 服务器异常
     * 30 填写的用户Id在服务器端不存在
     * 31 用户存在，但设备未绑定到该用户
     * 60 与服务端进行数据握手操作
     * 61 与服务端进行数据握手操作失败
     * 62 与服务端重新进行握手操作
     * 80 与手机端进行数据握手操作
     * 81 与手机端进行数据握手操作失败
     * 82 与手机端重新进行握手操作
     */
    private int networkStatus;

    /*
     * 底座电池电量
     */
    private int power;

    /*
     * WIFI 名称
     */
    private String wifiName;

    /*
     * WIFI 密码
     */
    private String wifiPassword;
}
使用示例
初始化：推荐与设备建立蓝牙连接后初始化SDK
private BleGattCallback bleGattCallback = new BleGattCallback() {
        @Override
        public void onStartConnect() {
        }

        @Override
        public void onConnectFail(final BleDevice device, BleException exception) {
        }

        @Override
        public void onConnectSuccess(BleDevice bleDevice, BluetoothGatt gatt, int status) {
            StarData.init(); // 这里初始化
        }
    };
设置数据输入、数据输出监听：与设备服务建立通信后
private BleNotifyCallback notifyCallBack = new BleNotifyCallback(){

        @Override
        public void onNotifySuccess() {
            StarData.setDataReceiver(dataReceiver); // 这里设置一个DataReceiver监听SDK解析后的数据， 如果不想实现DataReceiver的所有方法可以使用DataReceiverSample
            Log.d(TAG, "打开设备数据通知成功");
        }

        @Override
        public void onNotifyFailure(BleException exception) {
            Log.d(TAG, "打开设备数据通知失败-Exception:" + exception.getDescription());
        }

        @Override
        public void onCharacteristicChanged(byte[] bytes) {
            StarData.putData(bytes); // 这里把设备发送来的数据都交给SDK处理
        }
    };
private DataReceiver dataReceiver = new DataReceiverSample(){

        @Override
        public void onDataPoints(List<DataPoint> dataPointList) {
            // 接收波形数据点
        }

        ...
    }；
获取发送给设备的命令：SDK提供和设备交互的各种命令封装
send(CommandUtil.getDeviceStatus()); // 向设备发送获取系统状态命令
说明：send()是一个蓝牙通信方法，向设备发送数据；CommandUtil.getDeviceStatus()就是获取命令内容，发送完此命令后会触发DataReceiver.onDeviceStatus(DeviceStatus status)
 */
/**
 * SCI311W 心电仪数据源
 * 采样率：125
 */
class A0_HeartRateDataSource : BaseHeartRateDataSource() {
    override val protocol = Protocol(
        "00000001-0000-1000-8000-00805f9b34fb",
        "00000003-0000-1000-8000-00805f9b34fb",
        "00000002-0000-1000-8000-00805f9b34fb",
    )

    init {
        StarData.init()
    }

    override fun fetch(orderId: Long): Flow<HeartRate> = channelFlow {
        StarData.setDataReceiver(object : DataReceiverSample() {
            override fun onDataPoints(list: List<DataPoint>) {
                // DataPoint：心电数据点（采样率为125，1秒钟有125个点）。画波形使用这个就可以
                // 心率值
                val heartRate = list.lastOrNull()?.heartRate ?: 0
                // 心电图数据
                val coorYValues = if (list.isNullOrEmpty()) {
                    // 如果没有数据，就让心电图画y坐标为0的横线
                    (0..124).map { 0f }.toFloatArray()
                } else {
                    list.map {
                        /*
                        BigDecimal.setScale()方法用于格式化小数点
                        setScale(1)表示保留一位小数，默认用四舍五入方式
                        setScale(1,BigDecimal.ROUND_DOWN)直接删除多余的小数位，如2.35会变成2.3
                        setScale(1,BigDecimal.ROUND_UP)进位处理，2.35变成2.4
                        setScale(1,BigDecimal.ROUND_HALF_UP)四舍五入，2.35变成2.4
                        setScale(1,BigDecimal.ROUND_HALF_DOWN)四舍五入，2.35变成2.3，如果是5则向下舍
                         */
                        BigDecimal.valueOf(it.value.toDouble()).setScale(5, BigDecimal.ROUND_HALF_DOWN).toFloat() / 150f
                    }.toFloatArray()
                }
                trySend(HeartRate(value = heartRate, coorYValues = coorYValues, orderId = orderId))
            }

            override fun onAlertSwitch(alertSwitch: AlertSwitch?) {
                // 设备报警开关
                alertSwitch?.apply {
                    setLowPower(3)
                    setFlash(3)
                    setLeadOff(3)
                    setBleStatus(1)
                }
            }
        })
        Logger.i("A0_HeartRateDataSource setNotifyCallback")
        setNotifyCallback().collect {
            StarData.putData(it)
        }
    }.flowOn(Dispatchers.IO)

    override fun getSampleRate(): Int {
        return 125
    }

}
