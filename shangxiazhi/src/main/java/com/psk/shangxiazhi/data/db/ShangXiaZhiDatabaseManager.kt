package com.psk.shangxiazhi.data.db

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.psk.shangxiazhi.data.db.database.ShangXiaZhiDatabase

@SuppressLint("StaticFieldLeak")
object ShangXiaZhiDatabaseManager {
    private const val DB_NAME = "shangXiaZhi.db"
    private val MIGRATIONS = arrayOf(Migration1)
    private lateinit var context: Context
    val db: ShangXiaZhiDatabase by lazy {
        Room.databaseBuilder(context, ShangXiaZhiDatabase::class.java, DB_NAME)
            .addCallback(CreatedCallBack)
            .addMigrations(*MIGRATIONS)
            .build()
    }

    fun init(context: Context) {
        if (ShangXiaZhiDatabaseManager::context.isInitialized) {
            return
        }
        ShangXiaZhiDatabaseManager.context = context.applicationContext
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
