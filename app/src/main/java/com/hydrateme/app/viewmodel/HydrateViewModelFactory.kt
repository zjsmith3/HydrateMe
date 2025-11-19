package com.hydrateme.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.hydrateme.app.data.repository.HydrateRepository

class HydrateViewModelFactory(
    private val repository: HydrateRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HydrateViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HydrateViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
