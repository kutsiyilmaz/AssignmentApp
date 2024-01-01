package com.assignment.theapp.model


import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.assignment.theapp.viewmodel.TaskViewModel
import kotlinx.coroutines.coroutineScope

class RefreshWorker(context: Context, params: WorkerParameters,val viewModel: TaskViewModel) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = coroutineScope{
        try {
            viewModel.refreshData()
            Result.success()
        }catch (e: Exception) {
            Log.e("test", "Error in worker", e)
            Result.failure()
        }
    }
}



