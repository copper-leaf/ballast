package com.copperleaf.ballast.queue.driver.db

import org.jetbrains.exposed.v1.core.vendors.MysqlDialect
import org.jetbrains.exposed.v1.core.vendors.PostgreSQLDialect
import org.jetbrains.exposed.v1.core.vendors.currentDialect
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.JdbcTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

/**
 * This class is responsible for applying database migrations to the Exposed database. It does not track which
 * migrations have been applied, so it should only be used for testing and evaluation. It should not be used in
 * production code, you should use a proper migration tool like Flyway or Liquibase instead, using the migrations files
 * provided in the `migrations` resource directory.
 */
@Deprecated("This class should only be used for testing and evaluation. It should not be used in production code.")
public class ExposedDatabaseQueueMigrations(
    private val database: Database,
    private val table: JobsTable,
) {
    public suspend fun applyMigrations() {
        suspendTransaction(database) {
            applyV1()
        }
    }

    private suspend fun JdbcTransaction.applyV1() {
        val migrationResource = when (currentDialect) {
            is PostgreSQLDialect -> {
                this::class.java.classLoader
                    .getResource("migrations/postgres/V01_create_table.sql")
                    ?: error("Migration file not found")
            }

            is MysqlDialect -> {
                this::class.java.classLoader
                    .getResource("migrations/mysql/V01_create_table.sql")
                    ?: error("Migration file not found")
            }

            else -> {
                error("Unsupported database dialect: $currentDialect")
            }
        }

        migrationResource
            .readText()
            .replace($$"${tableName}", table.tableName)
            .split(";")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .forEach { exec(it) }
    }
}
