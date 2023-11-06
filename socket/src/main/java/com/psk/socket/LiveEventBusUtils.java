package com.psk.socket;

import androidx.lifecycle.Observer;

import com.google.gson.JsonObject;
import com.jeremyliao.liveeventbus.LiveEventBus;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class LiveEventBusUtils {
    private static final String KEY_SOCKET_SEND_DATA_TO_H5 = "key_socket_send_data_to_h5";
    private static final String KEY_SOCKET_SEND_DATA_TO_SERVER = "key_socket_send_data_to_server";

    private static class SingleTonInstance {
        private static final LiveEventBusUtils INSTANCE = new LiveEventBusUtils();
    }

    public static LiveEventBusUtils getInstance() {
        return SingleTonInstance.INSTANCE;
    }

    private LiveEventBusUtils() {
        LiveEventBus.get(KEY_SOCKET_SEND_DATA_TO_H5, Object.class).removeObserver(sendDataToH5Observer);
        LiveEventBus.get(KEY_SOCKET_SEND_DATA_TO_H5, Object.class).observeForever(sendDataToH5Observer);
        LiveEventBus.get(KEY_SOCKET_SEND_DATA_TO_SERVER, String.class).removeObserver(sendDataToServerObserver);
        LiveEventBus.get(KEY_SOCKET_SEND_DATA_TO_SERVER, String.class).observeForever(sendDataToServerObserver);
    }

    private Consumer<String> sendDataToServerCallback;
    private final Observer<String> sendDataToServerObserver = msg -> {
        LogsUtils.i("LiveEventBusUtils", "发送数据给服务器：" + msg);
        if (sendDataToServerCallback != null) {
            sendDataToServerCallback.accept(msg);
        }
    };
    private Consumer<Map<String, Object>> sendDataToH5Callback;
    private final Observer<Object> sendDataToH5Observer = obj -> {
        LogsUtils.i("LiveEventBusUtils", "发送数据给H5：" + obj);
        if (sendDataToH5Callback != null) {
            sendDataToH5Callback.accept((Map<String, Object>) obj);
        }
    };

    public void setSendDataToH5Callback(Consumer<Map<String, Object>> callback) {
        sendDataToH5Callback = callback;
    }

    public void setSendDataToServerCallback(Consumer<String> callback) {
        sendDataToServerCallback = callback;
    }

    /**
     * 发送socket连接状态给H5
     *
     * @param connectState socket连接状态(0 未连接、1 已连接)。
     */
    public static void sendSocketConnectStateToH5(int connectState) {
        sendDataToH5("connectState", connectState);
    }

    /**
     * 发送通话状态给H5
     *
     * @param callState 通话状态(0 开始拨打电话、1 电话接通(开始嘟嘟嘟)、2 电话挂断、3 录音文件被创建)。
     */
    public static void sendCallStateToH5(int callState) {
        sendDataToH5("callState", callState);
    }

    /**
     * 发送服务器传来的数据给H5
     */
    public static void sendServerDataToH5(Msg msg) {
        sendDataToH5("msg", msg);
    }

    /**
     * 发送接口错误信息给H5
     */
    public static void sendApiErrorToH5(int code, String msg) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("code", code);
        jsonObject.addProperty("msg", msg);
        sendDataToH5("apiError", jsonObject.toString());
    }

    /**
     * 发送数据给H5
     */
    private static void sendDataToH5(String key, Object data) {
        Map<String, Object> params = new HashMap<>();
        params.put(key, data);
        LiveEventBus.get(KEY_SOCKET_SEND_DATA_TO_H5).post(params);
    }

    public static void sendDataToServer(String msg) {
        LiveEventBus.get(KEY_SOCKET_SEND_DATA_TO_SERVER).post(msg);
    }

}
