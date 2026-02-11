package com.autologin.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auth_events")
data class AuthEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val userEmail: String,
    val userName: String,
    val timestamp: Long,
) {
    companion object {
        const val TYPE_LOGIN = "LOGIN"
        const val TYPE_LOGOUT = "LOGOUT"
    }
}
