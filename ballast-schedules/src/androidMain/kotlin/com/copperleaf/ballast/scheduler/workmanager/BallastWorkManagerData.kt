package com.copperleaf.ballast.scheduler.workmanager

import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import com.copperleaf.ballast.scheduler.SchedulerAdapter
import com.copperleaf.ballast.scheduler.workmanager.internal.getBooleanFromTag
import com.copperleaf.ballast.scheduler.workmanager.internal.getStringFromTag

internal data class BallastWorkManagerData(
    val adapter: SchedulerAdapter<*, *, *>,
    val callback: SchedulerCallback<*>,
    val withHistory: Boolean,
) {
    val adapterClassName: String = adapter::class.java.name
    val callbackClassName: String = callback::class.java.name

    fun applyToWorkRequestBuilder(builder: OneTimeWorkRequest.Builder) {
        builder.addTag("$DATA_ADAPTER_CLASS$adapterClassName")
        builder.addTag("$DATA_CALLBACK_CLASS$callbackClassName")
        builder.addTag("$DATA_WITH_HISTORY$withHistory")
    }

    fun applyToWorkRequestBuilder(builder: PeriodicWorkRequest.Builder) {
        builder.addTag("$DATA_ADAPTER_CLASS$adapterClassName")
        builder.addTag("$DATA_CALLBACK_CLASS$callbackClassName")
        builder.addTag("$DATA_WITH_HISTORY$withHistory")
    }

    override fun toString(): String {
        return "BallastWorkManagerData(" +
                "adapter=$adapterClassName, " +
                "callback=$callbackClassName, " +
                "withHistory=$withHistory)"
    }

    internal companion object {
        internal const val DATA_ADAPTER_CLASS: String = "ballast::DATA_ADAPTER_CLASS::"
        internal const val DATA_CALLBACK_CLASS: String = "ballast::DATA_CALLBACK_CLASS::"
        internal const val DATA_WITH_HISTORY: String = "ballast::DATA_WITH_HISTORY::"

        fun fromListenableWorker(worker: ListenableWorker): BallastWorkManagerData = with(worker) {
            return BallastWorkManagerData(
                adapter = (Class.forName(getStringFromTag(DATA_ADAPTER_CLASS)) as Class<SchedulerAdapter<*, *, *>>)
                    .getConstructor()
                    .newInstance(),
                callback = (Class.forName(getStringFromTag(DATA_CALLBACK_CLASS)) as Class<SchedulerCallback<*>>)
                    .getConstructor()
                    .newInstance(),
                withHistory = getBooleanFromTag(DATA_WITH_HISTORY, false),
            )
        }
    }
}
