-keep class com.psk.device.data.model.**{*;}
-keep public class * extends com.psk.device.data.source.remote.base.BaseBleDeviceDataSource{
    public <init>();# 不混淆构造方法，需要反射创建实例
}
-keep public class * extends com.psk.device.data.source.BaseBleDeviceRepository{
    public <init>();# 不混淆构造方法，需要反射创建实例
}
-keep class com.psk.device.data.source.remote.BleDataSourceFactory{
    public void foreach(***);# 不混淆 inline 方法
}