package com.autologin.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AuthEvent::class], version = 1, exportSchema = false)
abstract class AuthEventDatabase : RoomDatabase() {
    abstract fun authEventDao(): AuthEventDao
}
