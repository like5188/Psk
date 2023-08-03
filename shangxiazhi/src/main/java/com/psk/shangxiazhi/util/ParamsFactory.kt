package com.psk.shangxiazhi.util

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser

private val mGson by lazy {
    Gson()
}
private val mJsonParser by lazy {
    JsonParser()
}

/**
 * 把 [Map<String, Any?>] 类型的参数转换成 api 接口需要的 [JsonObject] 类型的参数。
 */
fun Map<String, Any?>.toJsonObjectForApi(): JsonObject {
    return mJsonParser.parse(mGson.toJson(this)).asJsonObject
}
