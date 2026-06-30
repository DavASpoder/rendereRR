package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "renderer_settings")
data class RendererSetting(
    @PrimaryKey val key: String,
    val value: Boolean
)

@Entity(tableName = "console_logs")
data class ConsoleLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String,
    val message: String,
    val level: String = "INFO" // INFO, SUCCESS, WARN, ERROR
)

@Entity(tableName = "registration_profiles")
data class RegistrationProfile(
    @PrimaryKey val profileId: String,
    val profileName: String,
    val targetSoC: String,
    val targetGpu: String,
    val ramAllocated: String,
    val active: Boolean
)
