package com.itsm.prototype

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        com.google.android.filament.Filament.init()

        System.loadLibrary("filament-jni")
        System.loadLibrary("gltfio-jni")
    }
}