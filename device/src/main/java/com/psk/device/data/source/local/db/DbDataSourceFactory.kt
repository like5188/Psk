package com.psk.device.data.source.local.db

import android.content.Context
import com.psk.device.DeviceType
import com.psk.device.data.db.dao.BaseDao
import com.psk.device.data.source.local.IDbDataSource
import com.psk.device.util.getSubclasses

/**
 * 数据库数据源工厂
 * 注意：如果要添加新的蓝牙设备系列（非血压、血氧、心电、上下肢），那么需要以下步骤：
 * 1、新增对应实体类放到[com.psk.device.data.model]中。
 * 2、新增一个对应的Dao。
 * 3、在[com.psk.device.data.db.database.DeviceDatabase]类中新增相关方法和entities。
 * 4、新增一个DbDataSource。名称格式为：[DeviceType]DbDataSource；包名为：[com.psk.device.data.source.local.db]。
 */
internal object DbDataSourceFactory {
    private lateinit var dataSourceClasses: List<Class<IDbDataSource<*>>>

    suspend fun init(context: Context) {
        if (DbDataSourceFactory::dataSourceClasses.isInitialized) {
            return
        }
        dataSourceClasses = IDbDataSource::class.java.getSubclasses(
            context,
            DbDataSourceFactory::class.java.`package`?.name
        )
    }

    inline fun foreach(block: (deviceTypeName: String, Class<IDbDataSource<*>>) -> Unit) {
        for (clazz in dataSourceClasses) {
            val deviceTypeName = clazz.simpleName.replace("DbDataSource", "")
            block(deviceTypeName, clazz)
        }
    }

    /**
     * 根据设备类型反射创建数据源
     */
    fun create(deviceType: DeviceType, dao: BaseDao<*>): IDbDataSource<*>? {
        foreach { deviceTypeName, clazz ->
            if (deviceTypeName == deviceType.name) {
                return try {
                    val constructor = clazz.getConstructor(BaseDao::class.java)
                    constructor.isAccessible = true
                    constructor.newInstance(dao)
                } catch (e: Exception) {
                    null
                }
            }
        }
        return null
    }

}
