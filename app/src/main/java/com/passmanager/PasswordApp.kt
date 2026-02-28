package com.passmanager

import android.app.Application
import com.passmanager.data.AppDatabase

class PasswordApp : Application() {
    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }
}
