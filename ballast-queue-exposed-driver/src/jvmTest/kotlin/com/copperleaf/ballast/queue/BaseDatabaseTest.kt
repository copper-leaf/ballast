package com.copperleaf.ballast.queue

import com.copperleaf.ballast.queue.driver.db.ExposedDatabaseQueueMigrations
import com.copperleaf.ballast.queue.driver.db.JobsTable
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.jdbc.Database
import org.testcontainers.containers.GenericContainer

abstract class BaseDatabaseTest {

    private suspend fun connectToPostgres(): Pair<GenericContainer<*>, Database> {
        val postgresContainer = GenericContainer("postgres:latest")
            .withExposedPorts(5432)
            .withEnv("POSTGRES_USER", "postgres")
            .withEnv("POSTGRES_PASSWORD", "postgres")
        postgresContainer.start()

        val host = postgresContainer.host
        val port = postgresContainer.firstMappedPort

        val database = Database.connect(
            "jdbc:postgresql://$host:$port/postgres",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "postgres"
        )

        ExposedDatabaseQueueMigrations(database, JobsTable.Default).applyMigrations()

        return postgresContainer to database
    }

    private suspend fun connectToMySql(): Pair<GenericContainer<*>, Database> {
        println("Connecting to mysql")

        val mysqlContainer = GenericContainer("mysql:latest")
            .withExposedPorts(3306)
            .withEnv("MYSQL_ROOT_PASSWORD", "mysql")
            .withEnv("MYSQL_DATABASE", "mysql")
            .withEnv("MYSQL_USER", "mysql")
            .withEnv("MYSQL_PASSWORD", "mysql")
        mysqlContainer.start()

        val host = mysqlContainer.host
        val port = mysqlContainer.firstMappedPort

        val database = Database.connect(
            "jdbc:mysql://$host:$port/mysql",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "mysql",
            password = "mysql"
        )

        ExposedDatabaseQueueMigrations(database, JobsTable.Default).applyMigrations()

        return mysqlContainer to database
    }

    internal fun runTestWithDatabase(block: suspend DatabaseTestScope.() -> Unit) = runTest {
        val (postgresContainer, postgresDatabase) = connectToPostgres()
        postgresContainer.use {
            block(DatabaseTestScope(this, postgresDatabase, JobsTable.Default))
        }

        val (mysqlContainer, mysqlDatabase) = connectToMySql()
        mysqlContainer.use {
            println("Running test with MySQL database at ${mysqlContainer.host}:${mysqlContainer.firstMappedPort}")
            block(DatabaseTestScope(this, mysqlDatabase, JobsTable.Default))
        }
    }

// Test Scope
// ---------------------------------------------------------------------------------------------------------------------

    class DatabaseTestScope(
        val testScope: TestScope,
        val database: Database,
        val table: JobsTable,
    )
}
