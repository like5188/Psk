package com.psk.shangxiazhi.data.source

import com.psk.shangxiazhi.data.model.LoginResult
import com.psk.shangxiazhi.data.source.remote.LoginRemoteDataSource

class LoginRepository(
    private val loginRemoteDataSource: LoginRemoteDataSource,
) {

    suspend fun login(phone: String?, password: String?, type: Int?): LoginResult? {
        return loginRemoteDataSource.load(phone, password, type)
    }

}