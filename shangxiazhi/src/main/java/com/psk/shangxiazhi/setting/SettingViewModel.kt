package com.psk.shangxiazhi.setting

import androidx.lifecycle.ViewModel
import com.psk.shangxiazhi.data.model.LoginResult
import com.psk.shangxiazhi.data.source.ShangXiaZhiRepository

class SettingViewModel(
    private val shangXiaZhiRepository: ShangXiaZhiRepository,
) : ViewModel() {

    suspend fun logout(patientToken: String?): LoginResult? {
        return shangXiaZhiRepository.logout(patientToken)
    }

}
