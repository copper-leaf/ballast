package com.copperleaf.ballast.examples.di

import androidx.compose.material3.SnackbarHostState
import com.copperleaf.ballast.examples.presentation.queue.MainQueueViewModel
import com.copperleaf.ballast.examples.presentation.ui.MainScreenEventHandler
import com.copperleaf.ballast.examples.presentation.ui.MainScreenInputHandler
import com.copperleaf.ballast.queue.driver.db.ExposedDatabaseQueueDriver
import com.copperleaf.ballast.queue.driver.db.JobsTable
import com.copperleaf.ballast.queue.driver.db.repository.JobsMaintenanceRepository
import com.copperleaf.ballast.queue.driver.db.repository.JobsMaintenanceRepositoryImpl
import com.copperleaf.ballast.queue.driver.db.repository.JobsRepository
import com.copperleaf.ballast.queue.driver.db.repository.JobsRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.TimeZone
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.jdbc.Database
import kotlin.random.Random
import kotlin.time.Clock

interface ComposeDesktopInjector {

    val clock: Clock
    val timezone: TimeZone
    val json: Json
    val snackbarHostState: SnackbarHostState

    val driver: ExposedDatabaseQueueDriver

    val mainQueueViewModel: MainQueueViewModel

    fun mainScreenInputHandler(): MainScreenInputHandler
    fun mainScreenEventHandler(): MainScreenEventHandler
}

class ComposeDesktopInjectorImpl(
    private val applicationCoroutineScope: CoroutineScope,
) : ComposeDesktopInjector {

    override val clock: Clock = Clock.System
    override val timezone: TimeZone = TimeZone.currentSystemDefault()
    override val json: Json = Json { prettyPrint = true }
    override val snackbarHostState: SnackbarHostState = SnackbarHostState()

    private val table: JobsTable = JobsTable.Default
    private val random: Random = Random

    private val postgresDatabase: Database = Database.connect(
        "jdbc:postgresql://localhost:5432/postgres",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "postgres"
    )
    private val mysqlDatabase: Database = Database.connect(
        "jdbc:mysql://localhost:3306/mysql",
        driver = "com.mysql.cj.jdbc.Driver",
        user = "mysql",
        password = "mysql"
    )
    val db = postgresDatabase

//        val db = mysqlDatabase
    private val jobsRepository: JobsRepository = JobsRepositoryImpl(db, table, clock, json, StdOutSqlLogger)
    private val jobsMaintenanceRepository: JobsMaintenanceRepository = JobsMaintenanceRepositoryImpl(
        db,
        table,
        StdOutSqlLogger,
    )
    override val driver: ExposedDatabaseQueueDriver = ExposedDatabaseQueueDriver(
        repository = jobsRepository,
    )

    override val mainQueueViewModel: MainQueueViewModel by lazy {
        MainQueueViewModel(
            applicationCoroutineScope,
            this
        )
    }

    override fun mainScreenInputHandler(): MainScreenInputHandler {
        return MainScreenInputHandler(jobsRepository, jobsMaintenanceRepository, mainQueueViewModel)
    }

    override fun mainScreenEventHandler(): MainScreenEventHandler {
        return MainScreenEventHandler(snackbarHostState)
    }
}
