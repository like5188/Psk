package com.psk.shangxiazhi.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.psk.device.RepositoryManager
import com.psk.device.data.model.OrderInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class HistoryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState = _uiState.asStateFlow()
    private val orderInfoRepository = RepositoryManager.orderInfoRepository
    private lateinit var datas: Map<String, List<OrderInfo>>// key:年月
    private val sdf = SimpleDateFormat("yyyy年MM月")

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val orderList = orderInfoRepository.getAll()
            if (orderList.isNullOrEmpty()) {
                return@launch
            }
            datas = orderList.groupBy {
                sdf.format(it.createTime)
            }
            _uiState.update {
                val key = datas.keys.lastOrNull()
                val value = datas[key]
                it.copy(
                    showTime = key, orderInfoList = value
                )
            }
        }
    }

    fun getPreTime() {
        if (!::datas.isInitialized) {
            return
        }
        val cur = _uiState.value.showTime
        if (cur.isNullOrEmpty()) {
            return
        }
        val dates = datas.keys.toList()
        if (dates.isEmpty()) {
            return
        }
        val index = dates.indexOf(cur)
        if (index < 0) {
            return
        }
        if (index - 1 >= 0) {
            _uiState.update {
                val key = dates[index - 1]
                val value = datas[key]
                it.copy(
                    showTime = key, orderInfoList = value
                )
            }
        }
    }

    fun getNextTime() {
        if (!::datas.isInitialized) {
            return
        }
        val cur = _uiState.value.showTime
        if (cur.isNullOrEmpty()) {
            return
        }
        val dates = datas.keys.toList()
        if (dates.isEmpty()) {
            return
        }
        val index = dates.indexOf(cur)
        if (index < 0) {
            return
        }
        if (index + 1 < dates.size) {
            _uiState.update {
                val key = dates[index + 1]
                val value = datas[key]
                it.copy(
                    showTime = key, orderInfoList = value
                )
            }
        }
    }

}