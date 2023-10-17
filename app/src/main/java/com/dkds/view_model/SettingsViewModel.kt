package com.dkds.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.dkds.data.dao.SettingsDao
import com.dkds.data.entity.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDao: SettingsDao
) : ViewModel() {
    val settings: LiveData<List<Settings>> = settingsDao.getAll()

    fun save(settings: Settings) {
        settingsDao.insert(settings)
    }
}
