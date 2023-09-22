package com.psk.shangxiazhi.main

import android.content.Context
import androidx.lifecycle.ViewModel
import com.psk.common.util.SecondCountDownTimer
import com.psk.shangxiazhi.data.source.ShangXiaZhiBusinessRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(KoinApiExtension::class)
class MainViewModel(
    private val shangXiaZhiBusinessRepository: ShangXiaZhiBusinessRepository,
) : ViewModel(), KoinComponent {
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()
    private val sdf: SimpleDateFormat by inject(named("yyyy-MM-dd HH:mm:ss"))
    private val countDownTimer by lazy {
        object : SecondCountDownTimer(Int.MAX_VALUE.toLong(), 1) {
            override fun onSecondTick(secondsUntilFinished: Long) {
                _uiState.update {
                    it.copy(
                        time = sdf.format(Date())
                    )
                }
            }

            override fun onFinish() {
            }
        }
    }

    init {
        countDownTimer.start()
    }

    fun isLogin(context: Context): Boolean {
        return shangXiaZhiBusinessRepository.isLogin(context)
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer.cancel()
    }

}
