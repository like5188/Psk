package com.psk.shangxiazhi.data.source

import com.psk.shangxiazhi.data.Api
import com.psk.shangxiazhi.util.RetrofitUtils

class LogoutDataSource {

    suspend fun load(patientToken: String?) {
        if (patientToken.isNullOrEmpty()) {
            throw IllegalArgumentException("patientToken is null or empty")
        }
        RetrofitUtils.getInstance().getService<Api>()
            .logout(patientToken)
            .getDataIfSuccess()
    }

}