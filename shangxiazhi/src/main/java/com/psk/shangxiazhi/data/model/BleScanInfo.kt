package com.psk.shangxiazhi.data.model

import com.like.recyclerview.model.IRecyclerViewItem
import com.psk.shangxiazhi.BR
import com.psk.shangxiazhi.R
import java.io.Serializable

class BleScanInfo(val name: String, val address: String) : IRecyclerViewItem, Serializable {
    override var variableId: Int = BR.bleScanInfo
    override var layoutId: Int = R.layout.item_ble_scan

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BleScanInfo

        if (address != other.address) return false

        return true
    }

    override fun hashCode(): Int {
        return address.hashCode()
    }

}