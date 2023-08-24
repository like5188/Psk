package com.psk.shangxiazhi.data.source

import com.psk.shangxiazhi.data.Api
import com.psk.shangxiazhi.data.model.LoginResult
import com.psk.shangxiazhi.util.RetrofitUtils

class LogoutDataSource {

    suspend fun load(patientToken: String?): LoginResult? {
        if (patientToken.isNullOrEmpty()) {
            throw IllegalArgumentException("patientToken is null or empty")
        }
        return RetrofitUtils.getInstance().getService<Api>()
            .logout(patientToken)
    }

}