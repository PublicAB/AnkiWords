// SPDX-License-Identifier: GPL-3.0-or-later
// SPDX-FileCopyrightText: Copyright (c) 2026 David

package com.ichi2.anki.pages

import android.content.Context
import com.ichi2.anki.R
import com.ichi2.anki.common.preferences.sharedPrefs
import com.ichi2.anki.common.storage.CollectionHelper
import timber.log.Timber
import java.io.File
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * 连续打卡记录：每天完成复习后调用 [record] 一次。
 *
 * 状态以 `review-streak.txt`（两行数字：最近打卡日 + 连续天数）存在 AnkiDroid 目录里，
 * 也就是和 collection.anki2 同一级的用户可见目录，因此：
 * - 覆盖安装（签名不变）不会丢；
 * - 卸载重装、debug/release 包互相覆盖也不会丢（SharedPreferences 会，这个目录不会）。
 *
 * SharedPreferences 只在旧版本数据迁移时读取，以及文件读写失败时兜底。
 */
object ReviewStreak {
    private const val PREF_LAST_DAY = "reviewStreakLastDay"
    private const val PREF_STREAK_DAYS = "reviewStreakDays"
    private const val FILE_NAME = "review-streak.txt"

    /**
     * 记录"今天已完成复习"，并返回提示文本（如"已连续打卡 3 天"）。
     * 当天重复调用不会累加；与上次打卡相隔超过一天则重新从 1 开始。
     */
    fun record(context: Context): String {
        val now = System.currentTimeMillis()
        val today = dayNumber(now)
        val (last, previous) = load(context)
        val streak =
            when (today - last) {
                0L -> previous.coerceAtLeast(1)
                1L -> previous + 1
                else -> 1
            }
        save(context, today, streak)
        return context.getString(R.string.review_streak_days, streak)
    }

    /** 本地时区下的"第几天"（自 1970-01-01 起算），避免使用 minSdk 24 不可用的 java.time */
    private fun dayNumber(millis: Long): Long = TimeUnit.MILLISECONDS.toDays(millis + TimeZone.getDefault().getOffset(millis))

    /** 打卡文件所在位置；存储目录未就绪时返回 null（退回 SharedPreferences） */
    private fun streakFile(context: Context): File? =
        try {
            File(CollectionHelper.getCurrentAnkiDroidDirectory(context), FILE_NAME)
        } catch (e: Exception) {
            Timber.w(e, "ReviewStreak: AnkiDroid directory unavailable")
            null
        }

    private fun load(context: Context): Pair<Long, Int> {
        val file = streakFile(context)
        if (file != null && file.exists()) {
            try {
                val parts = file.readText().trim().split(Regex("\\s+"))
                if (parts.size >= 2) {
                    val lastDay = parts[0].toLongOrNull()
                    val days = parts[1].toIntOrNull()
                    if (lastDay != null && days != null) return lastDay to days
                }
                Timber.w("ReviewStreak: ignoring malformed streak file")
            } catch (e: Exception) {
                Timber.w(e, "ReviewStreak: failed to read streak file")
            }
        }
        // 迁移旧版本存在 SharedPreferences 里的记录
        val prefs = context.sharedPrefs()
        return prefs.getLong(PREF_LAST_DAY, -1L) to prefs.getInt(PREF_STREAK_DAYS, 0)
    }

    private fun save(
        context: Context,
        lastDay: Long,
        days: Int,
    ) {
        context
            .sharedPrefs()
            .edit()
            .putLong(PREF_LAST_DAY, lastDay)
            .putInt(PREF_STREAK_DAYS, days)
            .apply()
        val file = streakFile(context) ?: return
        try {
            file.writeText("$lastDay $days\n")
        } catch (e: Exception) {
            Timber.w(e, "ReviewStreak: failed to write streak file")
        }
    }
}
