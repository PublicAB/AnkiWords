// SPDX-License-Identifier: GPL-3.0-or-later

import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    `kotlin-dsl`
    alias(libs.plugins.ktlint.gradle.plugin)
}

repositories {
    // Aliyun mirrors first (direct connectivity in CN); original repos kept as fallback
    // google()/gradlePluginPortal() last: dl.google.com hangs in CN and a repo network
    // error aborts the whole resolution, so unreachable fallbacks must come last
    maven { url = uri("https://maven.aliyun.com/repository/google") }
    maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    mavenCentral()
    google()
    gradlePluginPortal()
}

dependencies {
    // Needed by precompiled script plugins that apply `com.android.library`.
    // Pins AGP/KGP transitively, so call sites need `id("com.android.library")` etc. with no version.
    implementation("com.android.tools.build:gradle:${libs.versions.androidGradlePlugin.get()}")
    // Force the catalog version of KGP, otherwise it's overridden by AGP.
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:${libs.versions.kotlin.get()}")
}

configure<KtlintExtension> {
    version.set(libs.versions.ktlint.get())
}
