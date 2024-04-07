package com.psk.sixminutes.data.db

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.psk.sixminutes.data.db.database.SixMinutesDatabase

@SuppressLint("StaticFieldLeak")
object SixMinutesDatabaseManager {
    private const val DB_NAME = "sixMinutes.db"
    private val MIGRATIONS = arrayOf(Migration1)
    private lateinit var context: Context
    val db: SixMinutesDatabase by lazy {
        Room.databaseBuilder(context, SixMinutesDatabase::class.java, DB_NAME)
            .addCallback(CreatedCallBack)
            .addMigrations(*MIGRATIONS)
            .build()
    }

    fun init(context: Context) {
        if (SixMinutesDatabaseManager::context.isInitialized) {
            return
        }
        SixMinutesDatabaseManager.context = context.applicationContext
    }

    private object CreatedCallBack : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            //在新装app时会调用，调用时机为数据库build()之后，数据库升级时不调用此函数
            MIGRATIONS.map {
                Migration1.migrate(db)
            }
        }
    }

    private object Migration1 : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 数据库的升级语句
            // database.execSQL("")
        }
    }

}
