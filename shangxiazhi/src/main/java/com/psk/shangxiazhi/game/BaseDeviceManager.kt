package com.psk.shangxiazhi.game

import com.psk.device.data.source.IRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

abstract class BaseDeviceManager<T> {
    private var job: Job? = null
    abstract val repository: IRepository<T>

    fun startJob(lifecycleScope: CoroutineScope) {
        if (job != null) {
            return
        }
        job = lifecycleScope.launch(Dispatchers.IO) {
            handleFlow(repository.getFlow(this, 1))
        }
    }

    fun cancelJob() {
        job?.cancel()
        job = null
    }

    fun enable(name: String, address: String) {
        repository.enable(name, address)
    }

    protected abstract suspend fun handleFlow(flow: Flow<T>)

}
