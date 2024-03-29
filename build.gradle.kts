// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {11
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("com.google.gms.google-services") version "4.4.0" apply false
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.8.10" apply false
    id("com.android.library") version "8.2.0" apply false
    id("com.google.dagger.hilt.android") version "2.44" apply false
    id("com.google.firebase.crashlytics") version "2.9.9" apply false
}