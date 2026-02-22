CREATE TABLE IF NOT EXISTS jobs
(
    id                          BINARY(16) PRIMARY KEY,
    queue                       text                                     NOT NULL,
    original_queue              text        DEFAULT NULL                 NULL,
    payload                     JSON                                     NOT NULL,
    job_state                   JSON                                     NOT NULL,
    result_data                 JSON        DEFAULT (NULL)               NULL,
    priority                    INT         DEFAULT 0                    NOT NULL,
    run_at                      DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    max_attempts                INT         DEFAULT 5                    NOT NULL,
    retry_until                 DATETIME(6) DEFAULT NULL                 NULL,
    timeout_duration            BIGINT      DEFAULT '30000000000'        NOT NULL,
    lease_buffer_duration       BIGINT      DEFAULT '30000000000'        NOT NULL,
    leased_at                   DATETIME(6) DEFAULT NULL                 NULL,
    leased_until                DATETIME(6) DEFAULT NULL                 NULL,
    deduplication_key           text        DEFAULT NULL                 NULL,
    deduplication_duration      BIGINT      DEFAULT NULL                 NULL,
    unique_until                DATETIME(6) DEFAULT NULL                 NULL,
    message_group               text        DEFAULT NULL                 NULL,
    status                      VARCHAR(10) DEFAULT 'Pending'            NOT NULL,
    attempts                    INT         DEFAULT 0                    NOT NULL,
    last_run_result_type        VARCHAR(10) DEFAULT NULL                 NULL,
    last_run_finished_at        DATETIME(6) DEFAULT NULL                 NULL,
    last_run_duration           BIGINT      DEFAULT NULL                 NULL,
    last_run_failure_message    text        DEFAULT NULL                 NULL,
    last_run_failure_stacktrace text        DEFAULT NULL                 NULL,
    created_at                  DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    updated_at                  DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL,
    CONSTRAINT check_jobs_0 CHECK (status IN ('Pending', 'Running', 'Succeeded', 'Failed', 'Cooldown', 'Cancelled')),
    CONSTRAINT check_jobs_1 CHECK (last_run_result_type IN ('Success', 'Cancelled', 'Timeout', 'Failure'))
);
CREATE UNIQUE INDEX uniqueindex__jobs__unique_jobs ON jobs (queue(255), deduplication_key(255));
CREATE INDEX index__jobs__eligible_pending_jobs ON jobs (queue(255), status, priority, run_at);
CREATE INDEX index__jobs__age_expired ON jobs (status, last_run_finished_at);
CREATE INDEX index__jobs__cooldown_expired ON jobs (status, unique_until);
CREATE INDEX index__jobs__lease_timeout_expired ON jobs (status, leased_until);
