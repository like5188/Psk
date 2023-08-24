package com.psk.shangxiazhi.data.model

import com.google.gson.annotations.SerializedName

/**
 * 接口返回的数据模型
 */
data class ResultModel<T>(
    val code: Int = -1,
    @SerializedName("msg")
    val message: String? = null,
    @SerializedName("data", alternate = ["login", "user"])
    val data: T? = null,
) {
    companion object {
        const val CODE_SUCCESS = 0//成功
    }

    private fun isSuccess(): Boolean {
        return code == CODE_SUCCESS
    }

    /**
     * 成功返回的数据，否则抛异常。
     */
    fun getDataIfSuccess(): T? {
        if (!isSuccess()) {
            throw ResultModelException(
                code,
                if (message.isNullOrEmpty()) {
                    "unknown error"
                } else {
                    message
                }
            )
        }
        return data
    }

    /**
     * 成功并且返回值不为null，就返回数据，否则抛异常。
     */
    fun getNonNullDataIfSuccess(): T {
        return getDataIfSuccess() ?: throw ResultModelException(code, "data is null")
    }
}
