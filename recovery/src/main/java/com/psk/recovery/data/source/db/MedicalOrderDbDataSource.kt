package com.psk.recovery.data.source.db

import com.psk.recovery.data.db.dao.MedicalOrderDao
import com.psk.recovery.data.model.MedicalOrder

class MedicalOrderDbDataSource(
    private val medicalOrderDao: MedicalOrderDao
) {

    suspend fun update(vararg medicalOrders: MedicalOrder): Int {
        return medicalOrderDao.update(*medicalOrders)
    }

    suspend fun save(medicalOrder: MedicalOrder): Long {
        return medicalOrderDao.insert(medicalOrder).first()
    }

}
