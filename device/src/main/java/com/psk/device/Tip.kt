package com.psk.device

sealed class Tip(val msg: String)
class Normal(msg: String) : Tip(msg)
class Error(msg: String) : Tip(msg) {
    constructor(throwable: Throwable) : this(throwable.message ?: "unknown error")
}
