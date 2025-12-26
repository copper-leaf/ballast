package com.copperleaf.ballast.scheduler.schedule

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Month
import kotlinx.datetime.number
import kotlin.jvm.JvmName

public sealed class CronField {
    public abstract val min: Int
    public abstract val max: Int
    public abstract val wildcard: Boolean
    public abstract val values: List<Int>

    public fun matches(value: Int): Boolean {
        return (value in min..max) && (value in values)
    }

    public fun nextOrSame(value: Int): Int? {
        if (value !in min..max) return null
        return values.firstOrNull { it >= value }
    }
}

public class MonthField private constructor(
    override val min: Int,
    override val max: Int,
    override val values: List<Int>,
    override val wildcard: Boolean = false,
) : CronField() {

    public companion object {
        public const val MIN_VALUE: Int = 1
        public const val MAX_VALUE: Int = 12

        @JvmName("monthField_Int")
        public operator fun invoke(months: Iterable<Int>, wildcard: Boolean = false): MonthField {
            val values = months.distinct().sorted()
            require(values.isNotEmpty()) {
                "Month values must not be empty"
            }
            require(values.all { it in 1..12 }) {
                "Month values must all be between 1 and 12, got $values"
            }
            return MonthField(1, 12, values, wildcard)
        }

        public operator fun invoke(vararg months: Int, wildcard: Boolean = false): MonthField {
            return MonthField(months.toList(), wildcard)
        }

        @JvmName("monthField_Month")
        public operator fun invoke(months: Iterable<Month>, wildcard: Boolean = false): MonthField {
            return MonthField(months.map { it.number }, wildcard)
        }

        public operator fun invoke(vararg months: Month, wildcard: Boolean = false): MonthField {
            return MonthField(months.map { it.number }, wildcard)
        }

        public fun anyValue(step: Int = 1): MonthField {
            return MonthField(MIN_VALUE..MAX_VALUE step step, wildcard = true)
        }

        public fun exactValue(value: Int): MonthField {
            return MonthField(listOf(value), wildcard = false)
        }

        public fun range(min: Int, max: Int, step: Int = 1): MonthField {
            return MonthField(min..max step step, wildcard = false)
        }
    }
}

public class DayOfMonthField private constructor(
    override val min: Int,
    override val max: Int,
    override val values: List<Int>,
    override val wildcard: Boolean = false,
) : CronField() {

    public companion object {
        public const val MIN_VALUE: Int = 1
        public const val MAX_VALUE: Int = 31

        public operator fun invoke(days: Iterable<Int>, wildcard: Boolean = false): DayOfMonthField {
            val values = days.distinct().sorted()
            require(values.all { it in MIN_VALUE..MAX_VALUE }) {
                "Day-of-month values must all be between $MIN_VALUE and $MAX_VALUE, got $values"
            }
            return DayOfMonthField(MIN_VALUE, MAX_VALUE, values, wildcard)
        }

        public operator fun invoke(vararg days: Int, wildcard: Boolean = false): DayOfMonthField {
            return DayOfMonthField(days.toList(), wildcard)
        }

        public fun anyValue(step: Int = 1): DayOfMonthField {
            return DayOfMonthField(MIN_VALUE..MAX_VALUE step step, wildcard = true)
        }

        public fun exactValue(value: Int): DayOfMonthField {
            return DayOfMonthField(listOf(value), wildcard = false)
        }

        public fun range(min: Int, max: Int, step: Int = 1): DayOfMonthField {
            return DayOfMonthField(min..max step step, wildcard = false)
        }
    }
}

public class DayOfWeekField private constructor(
    override val min: Int,
    override val max: Int,
    override val values: List<Int>,
    override val wildcard: Boolean,
) : CronField() {

    public companion object {
        public const val MIN_VALUE: Int = 0
        public const val MAX_VALUE: Int = 6

        @JvmName("dayOfWeekField_Int")
        public operator fun invoke(days: Iterable<Int>, wildcard: Boolean = false): DayOfWeekField {
            val values = days.distinct().sorted()
            require(values.all { it in MIN_VALUE..MAX_VALUE }) {
                "Day-of-week values must all be between $MIN_VALUE and $MAX_VALUE, got $values"
            }
            return DayOfWeekField(MIN_VALUE, MAX_VALUE, values, wildcard)
        }

        public operator fun invoke(vararg days: Int, wildcard: Boolean = false): DayOfWeekField {
            return DayOfWeekField(days.toList(), wildcard)
        }

        @JvmName("dayOfWeekField_DayOfWeek")
        public operator fun invoke(days: Iterable<DayOfWeek>, wildcard: Boolean = false): DayOfWeekField {
            return DayOfWeekField(days.map { it.ordinal }, wildcard)
        }

        public operator fun invoke(vararg days: DayOfWeek, wildcard: Boolean = false): DayOfWeekField {
            return DayOfWeekField(days.map { it.ordinal }, wildcard)
        }

        public fun anyValue(step: Int = 1): DayOfWeekField {
            return DayOfWeekField(MIN_VALUE..MAX_VALUE step step, wildcard = true)
        }

        public fun exactValue(value: Int): DayOfWeekField {
            return DayOfWeekField(listOf(value), wildcard = false)
        }

        public fun range(min: Int, max: Int, step: Int = 1): DayOfWeekField {
            return DayOfWeekField(min..max step step, wildcard = false)
        }
    }
}

public class HourField private constructor(
    override val min: Int,
    override val max: Int,
    override val values: List<Int>,
    override val wildcard: Boolean,
) : CronField() {

    public companion object {
        public const val MIN_VALUE: Int = 0
        public const val MAX_VALUE: Int = 23

        public operator fun invoke(hours: Iterable<Int>, wildcard: Boolean = false): HourField {
            val values = hours.distinct().sorted()
            require(values.all { it in MIN_VALUE..MAX_VALUE }) {
                "Hour values must all be between $MIN_VALUE and $MAX_VALUE, got $values"
            }
            return HourField(MIN_VALUE, MAX_VALUE, values, wildcard)
        }

        public operator fun invoke(vararg hours: Int, wildcard: Boolean = false): HourField {
            return HourField(hours.toList(), wildcard)
        }

        public fun anyValue(step: Int = 1): HourField {
            return HourField(MIN_VALUE..MAX_VALUE step step, wildcard = true)
        }

        public fun exactValue(value: Int): HourField {
            return HourField(listOf(value), wildcard = false)
        }

        public fun range(min: Int, max: Int, step: Int = 1): HourField {
            return HourField(min..max step step, wildcard = false)
        }
    }
}

public class MinuteField private constructor(
    override val min: Int,
    override val max: Int,
    override val values: List<Int>,
    override val wildcard: Boolean,
) : CronField() {

    public companion object {
        public const val MIN_VALUE: Int = 0
        public const val MAX_VALUE: Int = 59

        public operator fun invoke(minutes: Iterable<Int>, wildcard: Boolean = false): MinuteField {
            val values = minutes.distinct().sorted()
            require(values.all { it in MIN_VALUE..MAX_VALUE }) {
                "Minute values must all be between $MIN_VALUE and $MAX_VALUE, got $values"
            }
            return MinuteField(MIN_VALUE, MAX_VALUE, values, wildcard)
        }

        public operator fun invoke(vararg minutes: Int, wildcard: Boolean = false): MinuteField {
            return MinuteField(minutes.toList(), wildcard)
        }

        public fun anyValue(step: Int = 1): MinuteField {
            return MinuteField(MIN_VALUE..MAX_VALUE step step, wildcard = true)
        }

        public fun exactValue(value: Int): MinuteField {
            return MinuteField(listOf(value), wildcard = false)
        }

        public fun range(min: Int, max: Int, step: Int = 1): MinuteField {
            return MinuteField(min..max step step, wildcard = false)
        }
    }
}
