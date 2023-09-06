package com.psk.shangxiazhi.history

import com.like.recyclerview.model.IRecyclerViewItem
import com.psk.shangxiazhi.BR
import com.psk.shangxiazhi.R

data class HistoryUiState(
    val showTime: String? = null,
    val dateAndDataList: List<DateAndData>? = null,
)

data class DateAndData(
    val year: Int? = null,
    val month: Int? = null,
    val day: Int? = null,
    val hour: Int? = null,
    val minute: Int? = null,
    val second: Int? = null,
    val data: Long? = null,
) : IRecyclerViewItem {
    override val layoutId: Int get() = R.layout.item_history
    override val variableId: Int get() = BR.item
}