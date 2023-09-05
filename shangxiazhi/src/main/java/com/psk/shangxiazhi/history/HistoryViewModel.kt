package com.psk.shangxiazhi.history

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psk.device.data.model.ShangXiaZhi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState = _uiState.asStateFlow()

    fun getDateList() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(
                    dateList = listOf(
                        "2019年05月",
                        "2020年01月",
                        "2020年03月",
                        "2020年04月",
                        "2020年08月",
                        "2020年11月",
                        "2021年03月",
                    )
                )
            }
        }
    }

    fun getShangXiaZhiListByMedicalOrderId(context: Context): List<ShangXiaZhi> {
        return emptyList()
    }

}