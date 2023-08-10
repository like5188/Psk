package com.psk.recovery.data.db.dao

import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update

/*
 * 如果返回值类型为 Flow<>
 * 1、Room 中的可观察查询有一项重要限制 - 只要对表中的任何行进行更新（无论该行是否在结果集中），查询就会重新运行。通过应用相应库中的 distinctUntilChanged() 运算符：流可以确保仅在实际查询结果发生更改时通知界面。
 *
 * 2、因为Room的Query注解需要一个常量，无法使用类似方法中的 $tableName 这样的变量，这里就无法通过泛型去解决，所以不能放到 BaseDao 中。
 * 但是如果使用了SupportSQLiteQuery类和@RawQuery进行原始sql语句查询，这样咱们就可以通过sql语句来封装一些通用的操作，就解决了Query注解无法直接使用泛型的问题。
 * 但是当 @RawQuery 方法返回可观察的类型[Flow]，但您需要使用注释中的observedEntities()字段指定在查询中访问哪些表。所以不能放到[BaseDao]中，因为不能使用泛型。
 * 会出现构建错误：Observable query return type (LiveData, Flowable, DataSource, DataSourceFactory etc) can only be used with SELECT queries that directly or indirectly (via @Relation, for example) access at least one table. For @RawQuery, you should specify the list of tables to be observed via the observedEntities field.
 */
abstract class BaseDao<T> {

    /**
     * @Insert 方法的每个参数都必须是一个带有 @Entity 注解的 Room 数据实体类实例，或是数据实体类实例的集合，而且每个参数都指向一个数据库 调用 @Insert 方法时，Room 会将每个传递的实体实例插入到相应的数据库表中。
     * 如果 @Insert 方法接收单个参数，则会返回 long 值，这是插入项的新 rowId。如果参数是数组或集合，则应改为返回由 long 值组成的数组或集合，并且每个值都作为其中一个插入项的 rowId。
     */
    @Insert
    abstract suspend fun insert(vararg t: T): List<Long>

    /**
     * Room 使用主键将传递的实体实例与数据库中的行进行匹配。如果没有具有相同主键的行，Room 不会进行任何更改。
     * @Update 方法可以选择性地返回 int 值，该值指示成功更新的行数。
     */
    @Update
    abstract suspend fun update(vararg t: T): Int

    /**
     * Room 使用主键将传递的实体实例与数据库中的行进行匹配。如果没有具有相同主键的行，Room 不会进行任何更改。
     * @Delete 方法可以选择性地返回 int 值，该值指示成功删除的行数。
     */
    @Delete
    abstract suspend fun delete(vararg t: T): Int

}
