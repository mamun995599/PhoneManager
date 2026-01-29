package com.phonemanager.service

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class KeepAliveService : JobService() {

    companion object {
        private const val TAG = "KeepAliveService"
        private const val JOB_ID = 1001
        private const val INTERVAL_MS = 15 * 60 * 1000L // 15 minutes

        fun schedule(context: Context) {
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

            val componentName = ComponentName(context, KeepAliveService::class.java)

            val jobInfo = JobInfo.Builder(JOB_ID, componentName)
                .setPersisted(true) // Survive reboots
                .setPeriodic(INTERVAL_MS)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                .build()

            val result = jobScheduler.schedule(jobInfo)
            Log.d(TAG, "Job scheduled: $result")
        }

        fun cancel(context: Context) {
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.cancel(JOB_ID)
            Log.d(TAG, "Job cancelled")
        }
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "KeepAliveService onStartJob")

        // Check if PhoneManagerService is running
        ensureServiceRunning()

        return false // Job is done
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "KeepAliveService onStopJob")
        return true // Reschedule if stopped
    }

    private fun ensureServiceRunning() {
        try {
            val intent = Intent(this, PhoneManagerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            Log.d(TAG, "PhoneManagerService started/restarted")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start PhoneManagerService", e)
        }
    }
}