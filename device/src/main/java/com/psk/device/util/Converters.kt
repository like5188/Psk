package com.psk.device.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.psk.device.data.model.BloodPressure

class Converters {
    private val gson: Gson by lazy {
        Gson()
    }

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

    @TypeConverter
    fun bloodPressureToString(value: BloodPressure?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun stringToBloodPressure(value: String?): BloodPressure? {
        return try {
            gson.fromJson(value, object : TypeToken<BloodPressure>() {}.type)
        } catch (e: Exception) {
            null
        }
    }

}