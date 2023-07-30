package com.vungn.attendancedemo.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.Calendar

class BackgroundCheckWorker(private val context: Context, workerParams: WorkerParameters) : Worker(
    context, workerParams
) {
    override fun doWork(): Result {
        val pref = context.getSharedPreferences(AUTO_START_PREF_KEY, Context.MODE_PRIVATE)
        val editor = pref.edit()
        editor.putString(AUTO_START_PREF_KEY, Calendar.getInstance().time.toString())
        editor.apply()
        return Result.success()
    }

    companion object {
        const val AUTO_START_PREF_KEY = "auto_start_pref"
    }
}
