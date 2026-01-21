package com.copperleaf.ballast.queue.driver.db

import org.jetbrains.exposed.v1.core.Expression
import org.jetbrains.exposed.v1.core.QueryBuilder
import org.jetbrains.exposed.v1.core.longLiteral
import org.jetbrains.exposed.v1.core.vendors.DatabaseDialect
import org.jetbrains.exposed.v1.core.vendors.MysqlDialect
import org.jetbrains.exposed.v1.core.vendors.PostgreSQLDialect
import kotlin.time.Duration
import kotlin.time.Instant

internal class TimestampAdd(
    private val start: Expression<Instant?>,
    private val duration: Duration,
    private val dialect: DatabaseDialect
) : Expression<Instant>() {
    override fun toQueryBuilder(queryBuilder: QueryBuilder) = queryBuilder {
        when (dialect) {
            is PostgreSQLDialect -> {
                // ($start + ($duration || ' seconds')::interval)
                append("(")
                append(start)
                append(" + (")
                append(longLiteral(duration.inWholeSeconds))
                append(" || ' seconds')::interval)")
            }
            is MysqlDialect -> {
                // DATE_ADD($start, INTERVAL $duration SECOND)
                append("DATE_ADD(")
                append(start)
                append(", INTERVAL ")
                append(longLiteral(duration.inWholeSeconds))
                append(" SECOND)")
            }
            else -> {
                error("Unsupported database dialect: $dialect")
            }
        }
    }
}
