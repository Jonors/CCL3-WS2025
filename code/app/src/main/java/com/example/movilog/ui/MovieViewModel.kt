package com.example.movilog.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilog.data.MovieRepository
import com.example.movilog.data.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MovieViewModel(private val repository: MovieRepository) : ViewModel() {

    // This is the "Truth" of what the screen should show
    private val _uiState = MutableStateFlow<List<Movie>>(emptyList())
    val uiState: StateFlow<List<Movie>> = _uiState.asStateFlow()

    init {
        fetchPopularMovies()
    }

    private fun fetchPopularMovies() {
        viewModelScope.launch {
            try {
                // Replace "YOUR_API_KEY" with your actual TMDB Key
                val token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI5YzgzOGIwYmI1ZjJmMjhhZDRmYTM3NzIwMjZjNWZkOSIsIm5iZiI6MTc2NzYyOTQ1OS43NjYsInN1YiI6IjY5NWJlMjkzZWM4NmQyZDY4ZjE3YmI0NyIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.j0chOgmWycwc9o23qJFXuzrkIoPyfVGZLPNra0-i6Us"
                val response = repository.fetchNewMovies(token)
                val movies = repository.fetchNewMovies(token)
                _uiState.value = movies?.results ?: emptyList()
            } catch (e: Exception) {
                // In a real app, you'd handle errors here (like showing a Toast)
            }
        }
    }
}