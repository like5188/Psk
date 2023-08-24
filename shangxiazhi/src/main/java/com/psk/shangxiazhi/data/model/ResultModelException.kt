package com.psk.shangxiazhi.data.model

/**
 * 接口返回false时，用此类封装，方便统一错误处理
 */
class ResultModelException(val code: Int, val errorMsg: String) : RuntimeException(errorMsg) {
    override fun toString(): String {
        return errorMsg
    }
}