package com.example.smart_city.manager

import android.content.Context
import android.content.SharedPreferences

object UserXpManager {

    private const val PREFS_NAME = "user_xp_prefs"
    private const val KEY_TOTAL_XP = "total_xp"
    private const val KEY_UPVOTED_REPORTS = "upvoted_reports"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getTotalXp(context: Context): Int =
        prefs(context).getInt(KEY_TOTAL_XP, 0)

    fun addXp(context: Context, amount: Int) {
        val current = getTotalXp(context)
        prefs(context).edit().putInt(KEY_TOTAL_XP, current + amount).apply()
    }

    fun hasUpvoted(context: Context, reportId: String): Boolean {
        val set = prefs(context).getStringSet(KEY_UPVOTED_REPORTS, emptySet()) ?: emptySet()
        return set.contains(reportId)
    }

    fun markUpvoted(context: Context, reportId: String) {
        val set = prefs(context).getStringSet(KEY_UPVOTED_REPORTS, emptySet())?.toMutableSet()
            ?: mutableSetOf()
        set.add(reportId)
        prefs(context).edit().putStringSet(KEY_UPVOTED_REPORTS, set).apply()
    }
}