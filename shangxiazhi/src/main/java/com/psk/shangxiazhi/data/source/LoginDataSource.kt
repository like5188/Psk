package com.psk.shangxiazhi.data.source

class LoginDataSource {

    /**
     * @param uuid  用户识别码
     * @param code  激活码
     */
    suspend fun load(uuid: String?, code: String?): Boolean {
        if (uuid.isNullOrEmpty()) {
            throw IllegalArgumentException("uuid is null or empty")
        }
        if (code.isNullOrEmpty()) {
            throw IllegalArgumentException("code is null or empty")
        }
        return uuid == code
    }

}