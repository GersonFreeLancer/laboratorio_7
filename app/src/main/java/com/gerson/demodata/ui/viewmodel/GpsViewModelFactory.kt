package com.gerson.demodata.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gerson.demodata.data.repository.GpsRepository

class GpsViewModelFactory(
    private val repository: GpsRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (modelClass.isAssignableFrom(GpsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GpsViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel")
    }
}