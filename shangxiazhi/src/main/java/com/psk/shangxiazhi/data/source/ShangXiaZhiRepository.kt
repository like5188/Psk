package com.psk.shangxiazhi.data.source

import com.psk.shangxiazhi.data.model.GetUserResult
import com.psk.shangxiazhi.data.model.LoginResult

class ShangXiaZhiRepository(
    private val loginDataSource: LoginDataSource,
    private val getUserDataSource: GetUserDataSource,
) {

    suspend fun login(phone: String?, password: String?, type: Int?): LoginResult? {
        return loginDataSource.load(phone, password, type)
    }

    suspend fun getUser(patientToken: String?): GetUserResult? {
        return getUserDataSource.load(patientToken)
    }

}