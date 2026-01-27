CREATE TABLE IF NOT EXISTS jobs
(
    id                          uuid PRIMARY KEY,
    queue                       TEXT                                  NOT NULL,
    payload                     JSONB                                 NOT NULL,
    job_state                   JSONB                                 NOT NULL,
    result_data                 JSONB       DEFAULT NULL::jsonb       NULL,
    priority                    INT         DEFAULT 0                 NOT NULL,
    run_at                      TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    max_attempts                INT         DEFAULT 5                 NOT NULL,
    retry_until                 TIMESTAMP   DEFAULT NULL              NULL,
    timeout_duration            BIGINT      DEFAULT '30000000000'     NOT NULL,
    lease_buffer_duration       BIGINT      DEFAULT '30000000000'     NOT NULL,
    leased_at                   TIMESTAMP   DEFAULT NULL              NULL,
    leased_until                TIMESTAMP   DEFAULT NULL              NULL,
    deduplication_key           TEXT        DEFAULT NULL              NULL,
    deduplication_duration      BIGINT      DEFAULT NULL              NULL,
    unique_until                TIMESTAMP   DEFAULT NULL              NULL,
    message_group               TEXT        DEFAULT NULL              NULL,
    status                      VARCHAR(10) DEFAULT 'Pending'         NOT NULL,
    attempts                    INT         DEFAULT 0                 NOT NULL,
    last_run_result_type        VARCHAR(10) DEFAULT NULL              NULL,
    last_run_finished_at        TIMESTAMP   DEFAULT NULL              NULL,
    last_run_duration           BIGINT      DEFAULT NULL              NULL,
    last_run_failure_message    TEXT        DEFAULT NULL              NULL,
    last_run_failure_stacktrace TEXT        DEFAULT NULL              NULL,
    created_at                  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at                  TIMESTAMP   DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT check_jobs_0 CHECK (status IN ('Pending', 'Running', 'Succeeded', 'Failed', 'Cooldown', 'Cancelled')),
    CONSTRAINT check_jobs_1 CHECK (last_run_result_type IN ('Success', 'Cancelled', 'Timeout', 'Failure'))
);
CREATE UNIQUE INDEX uniqueindex__jobs__unique_jobs ON jobs (queue, deduplication_key) WHERE
    (jobs.unique_until IS NOT NULL) AND (jobs.status IN ('Pending', 'Running', 'Cooldown'));
CREATE INDEX index__jobs__eligible_pending_jobs ON jobs (queue, status, priority, run_at) WHERE jobs.status = 'Pending';
CREATE INDEX index__jobs__age_expired ON jobs (status, last_run_finished_at) WHERE jobs.status = 'Succeeded';
CREATE INDEX index__jobs__cooldown_expired ON jobs (status, unique_until) WHERE jobs.status = 'Cooldown';
CREATE INDEX index__jobs__lease_timeout_expired ON jobs (status, leased_until) WHERE jobs.status = 'Running';
