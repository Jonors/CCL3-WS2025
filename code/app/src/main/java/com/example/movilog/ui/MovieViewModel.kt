package com.example.movilog.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilog.data.MovieRepository
import com.example.movilog.data.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MovieViewModel(private val repository: MovieRepository) : ViewModel() {

    // ✅ Hier dein TMDB v4 Read Access Token rein:
    private val token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI5YzgzOGIwYmI1ZjJmMjhhZDRmYTM3NzIwMjZjNWZkOSIsIm5iZiI6MTc2NzYyOTQ1OS43NjYsInN1YiI6IjY5NWJlMjkzZWM4NmQyZDY4ZjE3YmI0NyIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.j0chOgmWycwc9o23qJFXuzrkIoPyfVGZLPNra0-i6Us"

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _popularMovies = MutableStateFlow<List<Movie>>(emptyList())
    val popularMovies: StateFlow<List<Movie>> = _popularMovies.asStateFlow()

    private val _upcomingMovies = MutableStateFlow<List<Movie>>(emptyList())

    val upcomingMovies: StateFlow<List<Movie>> = _upcomingMovies.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Movie>>(emptyList())
    val searchResults: StateFlow<List<Movie>> = _searchResults.asStateFlow()

    init {
        fetchPopularMovies()
        fetchUpcomingMovies()
    }

    fun fetchPopularMovies() {
        viewModelScope.launch {
            try {
                val response = repository.fetchPopularMovies(token)
                _popularMovies.value = response.results
                Log.d("TMDB", "Popular loaded: ${response.results.size}")
            } catch (e: Exception) {
                _popularMovies.value = emptyList()
                Log.e("TMDB", "Failed to load popular movies", e)
            }
        }
    }

    fun fetchUpcomingMovies() {
        viewModelScope.launch {
            try {
                val response = repository.fetchUpcomingMovies(token)
                _upcomingMovies.value = response.results
                Log.d("TMDB", "Upcoming loaded: ${response.results.size}")
            } catch (e: Exception) {
                _upcomingMovies.value = emptyList()
                Log.e("TMDB", "Failed to load upcoming movies", e)
            }
        }
    }


    fun onQueryChange(newValue: String) {
        _query.value = newValue

        val trimmed = newValue.trim()
        if (trimmed.isEmpty()) {
            _searchResults.value = emptyList()
            return
        }

        // optional: erst ab 2 Zeichen suchen
        if (trimmed.length >= 2) {
            searchMovies(trimmed)
        }
    }

    private fun searchMovies(q: String) {
        viewModelScope.launch {
            try {
                val response = repository.searchMovies(token, q)
                _searchResults.value = response.results
                Log.d("TMDB", "Search '$q': ${response.results.size}")
            } catch (e: Exception) {
                _searchResults.value = emptyList()
                Log.e("TMDB", "Failed to search movies", e)
            }
        }
    }

    data class MovieDetailUiState(
        val isLoading: Boolean = true,
        val details: com.example.movilog.data.remote.MovieDetailsDto? = null,
        val inWatchlist: Boolean = false,
        val isWatched: Boolean = false,
        val userRating: Float? = null
    )

    private val _detailState = MutableStateFlow(MovieDetailUiState())
    val detailState: StateFlow<MovieDetailUiState> = _detailState.asStateFlow()

    fun loadMovieDetails(movieId: Int) {
        viewModelScope.launch {
            _detailState.value = MovieDetailUiState(isLoading = true)
            try {
                val details = repository.fetchMovieDetails(token, movieId)

                // TODO: später aus Room lesen (User-Status pro Movie)
                _detailState.value = MovieDetailUiState(
                    isLoading = false,
                    details = details,
                    inWatchlist = false,
                    isWatched = false,
                    userRating = null
                )
            } catch (e: Exception) {
                _detailState.value = MovieDetailUiState(isLoading = false, details = null)
            }
        }
    }

}
