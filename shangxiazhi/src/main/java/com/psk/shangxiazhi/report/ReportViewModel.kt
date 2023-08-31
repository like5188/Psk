package com.psk.shangxiazhi.report

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ReportViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState = _uiState.asStateFlow()


    companion object {
        private val TAG = ReportViewModel::class.java.simpleName
    }

}
