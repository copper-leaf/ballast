package com.copperleaf.ballast.queue.driver.db

import com.copperleaf.ballast.queue.JobCompletionResultType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inList
import org.jetbrains.exposed.v1.core.isNotNull
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.duration
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.json.jsonb
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.uuid.Uuid

/**
 * This class represents the "jobs" table in the database used for job queueing. It defines the schema of the
 * table, including columns and indexes, for efficiently querying and maintaining the job queue.
 *
 * It is an abstract class that can be extended to rename the table in your DB to something more appropriate for your
 * needs. By default, you can use the [JobsTable.Default] object which uses the table name "jobs".
 */
public abstract class JobsTable(tableName: String) : IdTable<Uuid>(tableName) {

    public object Default : JobsTable("jobs")

    // Columns
// ---------------------------------------------------------------------------------------------------------------------
    final override val id: Column<EntityID<Uuid>> = uuid("id")
        .databaseGenerated()
        .autoGenerate()
        .entityId()

    final override val primaryKey: PrimaryKey = PrimaryKey(id)

    // set at job creation
    public val queue: Column<String> = text("queue")

    public val payload: Column<JsonElement> = jsonb("payload", Json, JsonElement.serializer())
    public val job_state: Column<JsonElement> = jsonb("job_state", Json, JsonElement.serializer())
    public val result_data: Column<JsonElement?> = jsonb("result_data", Json, JsonElement.serializer())
        .nullable()
        .default(null)

    public val priority: Column<Int> = integer("priority")
        .default(0)
    public val run_at: Column<Instant> = timestamp("run_at")
        .databaseGenerated()
        .defaultExpression(CurrentTimestamp)
    public val max_attempts: Column<Int> = integer("max_attempts")
        .default(5)
    public val timeout_duration: Column<Duration> = duration("timeout_duration")
        .default(30.seconds)
    public val leased_at: Column<Instant?> = timestamp("leased_at")
        .nullable()
        .default(null)
    public val leased_until: Column<Instant?> = timestamp("leased_until")
        .nullable()
        .default(null)

    public val deduplication_key: Column<String?> = text("deduplication_key")
        .nullable()
        .default(null)
    public val deduplication_duration: Column<Duration?> = duration("deduplication_duration")
        .nullable()
        .default(null)
    public val unique_until: Column<Instant?> = timestamp("unique_until")
        .nullable()
        .default(null)

    // updated when a job is selected for processing
    public val status: Column<ExposedDatabaseJobStatus> =
        enumerationByName(
            name = "status",
            length = 10,
            klass = ExposedDatabaseJobStatus::class
        )
            .check { it inList ExposedDatabaseJobStatus.entries }
            .default(ExposedDatabaseJobStatus.Pending)
    public val attempts: Column<Int> = integer("attempts")
        .default(0)

    // set when a job is completed successfully or failed
    public val last_run_result_type: Column<JobCompletionResultType?> =
        enumerationByName(
            name = "last_run_result_type",
            length = 10,
            klass = JobCompletionResultType::class
        )
            .nullable()
            .check { it inList JobCompletionResultType.entries }
            .default(null)
    public val last_run_finished_at: Column<Instant?> = timestamp("last_run_finished_at")
        .nullable()
        .default(null)
    public val last_run_duration: Column<Duration?> = duration("last_run_duration")
        .nullable()
        .default(null)
    public val last_run_failure_message: Column<String?> = text("last_run_failure_message")
        .nullable()
        .default(null)
    public val last_run_failure_stacktrace: Column<String?> = text("last_run_failure_stacktrace")
        .nullable()
        .default(null)

    public val created_at: Column<Instant> = timestamp("created_at")
        .databaseGenerated()
        .defaultExpression(CurrentTimestamp)
    public val updated_at: Column<Instant> = timestamp("updated_at")
        .databaseGenerated()
        .defaultExpression(CurrentTimestamp)

// Indexes
// ---------------------------------------------------------------------------------------------------------------------

    /**
     * Index to enforce uniqueness of jobs with a deduplication key that are still considered "unique" (i.e., their
     * uniqueness has not expired). This prevents multiple identical jobs from being enqueued simultaneously.
     *
     * Jobs with the same [deduplication_key] are unique until the [unique_until] has passed, while they are in one of
     * the following states:
     *
     * - [ExposedDatabaseJobStatus.Pending]: The job is enqueued. Don't enqueue another, even if it's run_at would be later
     *   than this jobs's [unique_until], since it's possible that this job fails and will get scheduled for retry.
     * - [ExposedDatabaseJobStatus.Running]: The unique job has been selected for processing. Don't enqueue another, since
     *   it's possible that this job fails and will get scheduled for retry.
     * - [ExposedDatabaseJobStatus.Cooldown]: The job has completed, but is now in cooldown mode. A maintenance task will
     *   eventually move this job's [state] to [ExposedDatabaseJobStatus.Succeeded] once the cooldown period has expired. Until
     *   it has actually been moved to Succeeded, we must still consider it unique.
     */
    private val uniqueindex__jobs__unique_jobs = index(
        "uniqueindex__${tableName}__unique_jobs",
        true,
        *arrayOf(queue, deduplication_key),
    ) {
        unique_until.isNotNull() and
                (status inList listOf(ExposedDatabaseJobStatus.Pending, ExposedDatabaseJobStatus.Running, ExposedDatabaseJobStatus.Cooldown))
    }

    /**
     * Index to efficiently query for pending jobs that are ready to be processed, ordered by priority and scheduled
     * run time.
     *
     * @see [com.copperleaf.ballast.queue.driver.db.repository.JobsRepository.claimNextAvailableJob]
     */
    private val index__jobs__eligible_pending_jobs = index(
        "index__${tableName}__eligible_pending_jobs",
        false,
        *arrayOf(queue, status, priority, run_at),
    ) { status eq ExposedDatabaseJobStatus.Pending }

    /**
     * Index to efficiently find completed jobs eligible for deletion by a maintenance task.
     *
     * @see [com.copperleaf.ballast.queue.driver.db.repository.JobsMaintenanceRepository.deleteOldJobs]
     */
    private val index__jobs__age_expired = index(
        "index__${tableName}__age_expired",
        false,
        *arrayOf(status, last_run_finished_at),
    ) { status eq ExposedDatabaseJobStatus.Succeeded }

    /**
     * Index to efficiently find jobs that are in cooldown mode, but beyond their [unique_until] time. These jobs
     * can be moved to [ExposedDatabaseJobStatus.Succeeded] by a maintenance task.
     *
     * @see [com.copperleaf.ballast.queue.driver.db.repository.JobsMaintenanceRepository.freeJobCooldowns]
     */
    private val index__jobs__cooldown_expired = index(
        "index__${tableName}__cooldown_expired",
        false,
        *arrayOf(status, unique_until),
    ) { status eq ExposedDatabaseJobStatus.Cooldown }

    /**
     * Index to efficiently find running jobs that have exceeded their lease period, and are eligible to be retried.
     *
     * @see [com.copperleaf.ballast.queue.driver.db.repository.JobsMaintenanceRepository.retryHungJobs]
     */
    private val index__jobs__lease_timeout_expired = index(
        "index__${tableName}__lease_timeout_expired",
        false,
        *arrayOf(status, leased_until),
    ) { (status eq ExposedDatabaseJobStatus.Running) }
}
