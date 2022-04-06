package com.copperleaf.ballast.android.util

import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.copperleaf.ballast.android.MainApplication
import com.copperleaf.ballast.examples.scorekeeper.prefs.ScoreKeeperPrefs

@Suppress("DEPRECATION")
class ScorekeeperPrefsImpl : ScoreKeeperPrefs {
    private val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(
        MainApplication.getInstance()
    )

    override var buttonValues: List<Int>
        get() {
            return prefs
                .getStringSet("buttonValues", setOf("1", "5", "10"))
                ?.map { it.toInt() }
                ?: listOf(1, 5, 10)
        }
        set(value) {
            val mappedValues = value.mapTo(mutableSetOf()) { it.toString() }
            prefs
                .edit()
                .putStringSet("buttonValues", mappedValues)
                .apply()
        }

    override var scoresheetState: Map<String, Int>
        get() {
            return prefs
                .getStringSet("scoresheetState", emptySet())
                ?.associate { value ->
                    val (first, second) = value.trimStart('[').trimEnd(']').split("]:[")
                    first to second.toInt()
                }
                ?: emptyMap()
        }
        set(value) {
            val mappedValues = value
                .entries
                .mapTo(mutableSetOf()) { (key, value) -> "[$key]:[$value]" }

            prefs
                .edit()
                .putStringSet("scoresheetState", mappedValues)
                .apply()
        }
}
