package com.psk.shangxiazhi.data.source

import com.psk.shangxiazhi.data.Api
import com.psk.shangxiazhi.util.RetrofitUtils

class GetUserDataSource {

    suspend fun load(patientToken: String?): String? {
        if (patientToken.isNullOrEmpty()) {
            throw IllegalArgumentException("patientToken is null or empty")
        }
        return RetrofitUtils.getInstance().getService<Api>()
            .getUser(patientToken)
    }

}