package com.assignment.theapp.model

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.assignment.theapp.viewmodel.TaskViewModel

class RefreshWorkerFactory(private val viewModel: TaskViewModel) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when(workerClassName) {
            RefreshWorker::class.java.name -> RefreshWorker(appContext, workerParameters, viewModel)
            else -> null
        }
    }
}
