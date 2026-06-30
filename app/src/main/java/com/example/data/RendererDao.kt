package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RendererDao {
    @Query("SELECT * FROM renderer_settings")
    fun getAllSettings(): Flow<List<RendererSetting>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: RendererSetting)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: List<RendererSetting>)

    @Query("SELECT * FROM console_logs ORDER BY timestamp ASC")
    fun getAllLogs(): Flow<List<ConsoleLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ConsoleLog)

    @Query("DELETE FROM console_logs")
    suspend fun clearLogs()

    @Query("SELECT * FROM registration_profiles")
    fun getAllProfiles(): Flow<List<RegistrationProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: RegistrationProfile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<RegistrationProfile>)

    @Query("UPDATE registration_profiles SET active = (profileId = :profileId)")
    suspend fun setActiveProfile(profileId: String)
}
