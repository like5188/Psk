-keep class com.psk.device.data.model.**{*;}
-keep public class * extends com.psk.device.data.source.remote.ble.base.BaseBleDeviceDataSource{
    public <init>();# 不混淆构造方法，需要反射创建实例
}
-keep public class * extends com.psk.device.data.source.remote.socket.base.BaseSocketDeviceDataSource{
    public <init>();# 不混淆构造方法，需要反射创建实例
}
-keep public class * extends com.psk.device.data.source.repository.ble.BaseBleDeviceRepository{
    public <init>();# 不混淆构造方法，需要反射创建实例
}
-keep public class * extends com.psk.device.data.source.repository.socket.BaseSocketDeviceRepository{
    public <init>();# 不混淆构造方法，需要反射创建实例
}
-keep class com.psk.device.data.source.remote.ble.BleDataSourceFactory{
    public void foreach(***);# 不混淆 inline 方法
}
-keep class com.psk.device.data.source.remote.socket.SocketDataSourceFactory{
    public void foreach(***);# 不混淆 inline 方法
}