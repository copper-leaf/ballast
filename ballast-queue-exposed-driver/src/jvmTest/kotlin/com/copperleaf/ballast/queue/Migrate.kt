package com.copperleaf.ballast.queue

import com.copperleaf.ballast.queue.driver.db.JobsTable
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.core.ExperimentalDatabaseMigrationApi
import org.jetbrains.exposed.v1.core.InternalApi
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.migration.jdbc.MigrationUtils.statementsRequiredForDatabaseMigration
import java.io.File
import kotlin.test.Ignore
import kotlin.test.Test

@Ignore
class Migrate {

    @OptIn(ExperimentalDatabaseMigrationApi::class)
    @Test
    fun createPostgresMigrationScript() = runTest {
        val postgresqldb = Database.connect(
            "jdbc:postgresql://localhost:5432/postgres",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "postgres"
        )
        suspendTransaction(postgresqldb) {
            generateMigrationScript(
                JobsTable.Default,
                scriptDirectory = ".",
                scriptName = "postgresql_jobs",
            ).also {
                println(it)
            }
        }

    }

    @OptIn(ExperimentalDatabaseMigrationApi::class)
    @Test
    fun createMysqlMigrationScript() = runTest {
        val mysqlDb = Database.connect(
            "jdbc:mysql://localhost:3306/mysql",
            driver = "com.mysql.cj.jdbc.Driver",
            user = "mysql",
            password = "mysql"
        )
        suspendTransaction(mysqlDb) {
            generateMigrationScript(
                JobsTable.Default,
                scriptDirectory = ".",
                scriptName = "mysql_jobs",
            ).also {
                println(it)
            }
        }
    }

    private fun generateMigrationScript(
        vararg tables: Table,
        scriptDirectory: String,
        scriptName: String,
        withLogs: Boolean = true
    ): File {
        require(tables.isNotEmpty()) { "Tables argument must not be empty" }

        val allStatements = statementsRequiredForDatabaseMigration(*tables, withLogs = withLogs)

        @OptIn(InternalApi::class)
        return allStatements.writeMigrationScriptTo("$scriptDirectory/$scriptName.sql")
    }

    protected fun List<String>.writeMigrationScriptTo(filePath: String): File {
        val migrationScript = File(filePath)
        migrationScript.createNewFile()
        // Clear existing content
        migrationScript.writeText("")
        // Append statements
        forEach { statement ->
            // Add semicolon only if it's not already there
            val conditionalSemicolon = if (statement.lastOrNull() == ';') "" else ";"
            migrationScript.appendText("$statement$conditionalSemicolon\n")
        }
        return migrationScript
    }
}
