package com.psk.common.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun FloatArray.asFlow(): Flow<Float> = flow {
    forEach { value ->
        emit(value)
    }
}
