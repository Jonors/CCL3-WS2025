package com.example.movilog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.movilog.data.repository.MovieRepository
import com.example.movilog.util.ConnectivityObserver

class MovieViewModelFactory(
    private val repository: MovieRepository,
    private val connectivityObserver: ConnectivityObserver // ADD THIS
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovieViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MovieViewModel(repository, connectivityObserver) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}