package com.psk.shangxiazhi.history

/**
 * 把整数转换成2位。比如:1->"01"
 */
fun Int?.format2(): String {
    this ?: return ""
    return if (this < 10) {
        "0$this"
    } else {
        this.toString()
    }
}