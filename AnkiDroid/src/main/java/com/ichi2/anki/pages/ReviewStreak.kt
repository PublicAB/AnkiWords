// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: Copyright (c) 2026 David

package com.ichi2.anki.pages

import android.content.Context
import com.ichi2.anki.R
import com.ichi2.anki.common.preferences.sharedPrefs
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * 连续打卡记录：每天完成复习后调用 [record] 一次，
 * 只往默认 SharedPreferences 里存两个字段（最近打卡日期 + 连续天数），
 * 仅用于展示提示，不影响任何学习流程。
 */
object ReviewStreak {
    private const val PREF_LAST_DAY = "reviewStreakLastDay"
    private const val PREF_STREAK_DAYS = "reviewStreakDays"

    /**
     * 记录"今天已完成复习"，并返回提示文本（如"已连续打卡 3 天"）。
     * 当天重复调用不会累加；与上次打卡相隔超过一天则重新从 1 开始。
     */
    fun record(context: Context): String {
        val prefs = context.sharedPrefs()
        val now = System.currentTimeMillis()
        // 用本地时区算出"今天是第几天"（自 1970-01-01 起的天数），避免 java.time（minSdk 24 不可用）
        val today = TimeUnit.MILLISECONDS.toDays(now + TimeZone.getDefault().getOffset(now))
        val last = prefs.getLong(PREF_LAST_DAY, -1L)
        val streak =
            when (today - last) {
                0L -> prefs.getInt(PREF_STREAK_DAYS, 1).coerceAtLeast(1)
                1L -> prefs.getInt(PREF_STREAK_DAYS, 0) + 1
                else -> 1
            }
        prefs
            .edit()
            .putLong(PREF_LAST_DAY, today)
            .putInt(PREF_STREAK_DAYS, streak)
            .apply()
        return context.getString(R.string.review_streak_days, streak)
    }
}
