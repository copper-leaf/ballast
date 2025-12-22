package com.copperleaf.ballast.scheduler.schedule

public sealed interface CronField : CronValue

public class MinuteField(
    value: CronValue
) : CronField, CronValue by value {
    init {
        require(value.min == 0 && value.max == 59)
    }
}

public class HourField(
    value: CronValue
) : CronField, CronValue by value {
    init {
        require(value.min == 0 && value.max == 23)
    }
}

public class DayOfMonthField(
    value: CronValue
) : CronField, CronValue by value {
    init {
        require(value.min == 1 && value.max == 31)
    }
}

public class MonthField(
    value: CronValue
) : CronField, CronValue by value {
    init {
        require(value.min == 1 && value.max == 12)
    }
}

public class DayOfWeekField(
    value: CronValue
) : CronField, CronValue by value {
    init {
        require(value.min == 0 && value.max == 6)
    }
}

