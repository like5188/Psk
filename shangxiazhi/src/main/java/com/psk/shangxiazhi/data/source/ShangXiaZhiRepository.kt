package com.psk.shangxiazhi.data.source

import com.psk.shangxiazhi.data.model.Login
import com.psk.shangxiazhi.data.model.User

class ShangXiaZhiRepository(
    private val loginDataSource: LoginDataSource,
    private val logoutDataSource: LogoutDataSource,
    private val getUserDataSource: GetUserDataSource,
) {

    suspend fun login(phone: String?, password: String?, type: Int?): Login? {
        return loginDataSource.load(phone, password, type)
    }

    suspend fun logout(patientToken: String?) {
        logoutDataSource.load(patientToken)
    }

    suspend fun getUser(patientToken: String?): User? {
        return getUserDataSource.load(patientToken)
    }

}