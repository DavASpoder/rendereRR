package com.example.data

import kotlinx.coroutines.flow.Flow

class RendererRepository(private val dao: RendererDao) {
    val allSettings: Flow<List<RendererSetting>> = dao.getAllSettings()
    val allLogs: Flow<List<ConsoleLog>> = dao.getAllLogs()
    val allProfiles: Flow<List<RegistrationProfile>> = dao.getAllProfiles()

    suspend fun updateSetting(key: String, value: Boolean) {
        dao.insertSetting(RendererSetting(key, value))
    }

    suspend fun insertInitialSettings(settings: List<RendererSetting>) {
        dao.insertSettings(settings)
    }

    suspend fun addLog(tag: String, message: String, level: String = "INFO") {
        dao.insertLog(ConsoleLog(tag = tag, message = message, level = level))
    }

    suspend fun clearLogs() {
        dao.clearLogs()
    }

    suspend fun insertProfile(profile: RegistrationProfile) {
        dao.insertProfile(profile)
    }

    suspend fun insertProfiles(profiles: List<RegistrationProfile>) {
        dao.insertProfiles(profiles)
    }

    suspend fun setActiveProfile(profileId: String) {
        dao.setActiveProfile(profileId)
    }
}
