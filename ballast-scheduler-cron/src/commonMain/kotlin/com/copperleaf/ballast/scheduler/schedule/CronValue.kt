package com.copperleaf.ballast.scheduler.schedule

public sealed interface CronValue {
    public val min: Int
    public val max: Int
    public val isWildcard: Boolean

    public fun matches(value: Int): Boolean

    /**
     * Returns the smallest allowed value >= input, or null if none exists
     * in this field’s range.
     */
    public fun nextOrSame(value: Int): Int?
}

// Raw Field values
// ---------------------------------------------------------------------------------------------------------------------

public data class AnyValue(
    override val min: Int,
    override val max: Int,
    val step: Int = 1
) : CronValue {

    override val isWildcard: Boolean = true

    override fun matches(value: Int): Boolean =
        value in min..max && (value - min) % step == 0

    override fun nextOrSame(value: Int): Int? {
        if (value !in min..max) return null
        val offset = ((value - min + step - 1) / step) * step
        val result = min + offset
        return result.takeIf { it in min..max }
    }
}

public data class ExactValue(
    override val min: Int,
    override val max: Int,
    val value: Int
) : CronValue {

    init {
        require(value in min..max)
    }

    override val isWildcard: Boolean = false

    override fun matches(value: Int): Boolean =
        this.value == value

    override fun nextOrSame(value: Int): Int? =
        this.value.takeIf { it >= value }
}

public data class RangeValue(
    override val min: Int,
    override val max: Int,
    val start: Int,
    val end: Int,
    val step: Int = 1
) : CronValue {

    init {
        require(start in min..max)
        require(end in min..max)
        require(start <= end)
        require(step > 0)
    }

    override val isWildcard: Boolean = false

    override fun matches(value: Int): Boolean =
        value in start..end && (value - start) % step == 0

    override fun nextOrSame(value: Int): Int? {
        if (value > end) return null
        val base = maxOf(value, start)
        val offset = ((base - start + step - 1) / step) * step
        val result = start + offset
        return result.takeIf { it <= end }
    }
}

public data class ListValue(
    val fields: List<CronValue>
) : CronValue {

    override val min: Int = fields.minOf { it.min }
    override val max: Int = fields.maxOf { it.max }
    override val isWildcard: Boolean = false

    override fun matches(value: Int): Boolean =
        fields.any { it.matches(value) }

    override fun nextOrSame(value: Int): Int? =
        fields.mapNotNull { it.nextOrSame(value) }.minOrNull()
}
