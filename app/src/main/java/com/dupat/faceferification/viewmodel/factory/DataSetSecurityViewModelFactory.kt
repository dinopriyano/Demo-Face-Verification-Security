package com.dupat.faceferification.viewmodel.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dupat.faceferification.repositories.SecurityDatabaseRepository
import com.dupat.faceferification.viewmodel.DataSetSecurityViewModel

class DataSetSecurityViewModelFactory(private val repository: SecurityDatabaseRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(DataSetSecurityViewModel::class.java)){
            return DataSetSecurityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown View Model class")
    }

}