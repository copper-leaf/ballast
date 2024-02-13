package com.copperleaf.ballast.scheduler.vm

import com.copperleaf.ballast.Queued
import com.copperleaf.ballast.scheduler.SchedulerAdapter

public object SchedulerContract {
    public data class State<I : Any, E : Any, S : Any>(
        val schedules: Map<String, ScheduleState> = emptyMap()
    )

    public sealed interface Inputs<I : Any, E : Any, S : Any> {
        public class StartSchedules<I : Any, E : Any, S : Any>(
            public val adapter: SchedulerAdapter<I, E, S>
        ) : Inputs<I, E, S>

        public class PauseSchedule<I : Any, E : Any, S : Any>(
            public val key: String
        ) : Inputs<I, E, S>

        public class ResumeSchedule<I : Any, E : Any, S : Any>(
            public val key: String
        ) : Inputs<I, E, S>

        public class CancelSchedule<I : Any, E : Any, S : Any>(
            public val key: String
        ) : Inputs<I, E, S>

        public class MarkScheduleComplete<I : Any, E : Any, S : Any>(
            public val key: String
        ) : Inputs<I, E, S>

        public class DispatchScheduledTask<I : Any, E : Any, S : Any>(
            public val key: String,
            public val queued: Queued.HandleInput<I, E, S>,
        ) : Inputs<I, E, S>

        public class ScheduledTaskDropped<I : Any, E : Any, S : Any>(
            public val key: String,
        ) : Inputs<I, E, S>

        public class ScheduledTaskFailed<I : Any, E : Any, S : Any>(
            public val key: String,
        ) : Inputs<I, E, S>
    }

    public sealed interface Events<I : Any, E : Any, S : Any> {
        public data class PostInputToHost<I : Any, E : Any, S : Any>(
            val queued: Queued.HandleInput<I, E, S>,
        ) : Events<I, E, S>
    }
}
