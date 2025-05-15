plugins {
    alias(libs.plugins.android.application) apply false
}

buildscript {
    dependencies { classpath(libs.google.services) }
}