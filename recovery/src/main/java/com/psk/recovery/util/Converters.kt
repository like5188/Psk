package com.psk.recovery.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@OptIn(KoinApiExtension::class)
class Converters : KoinComponent {
    private val gson: Gson by inject()

    @TypeConverter
    fun intArrayToString(value: IntArray?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun stringToIntArray(value: String?): IntArray? {
        return try {
            gson.fromJson(value, object : TypeToken<IntArray>() {}.type)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun floatArrayToString(value: FloatArray?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun stringToFloatArray(value: String?): FloatArray? {
        return try {
            gson.fromJson(value, object : TypeToken<FloatArray>() {}.type)
        } catch (e: Exception) {
            null
        }
    }

}