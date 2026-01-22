package com.example.movilog.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movilog.data.model.CustomList
import com.example.movilog.data.model.ListWithMovies
import com.example.movilog.data.model.Movie
import com.example.movilog.data.remote.MovieDetailsDto
import com.example.movilog.data.repository.MovieRepository
import com.example.movilog.ui.stats.StatsUiState
import com.example.movilog.util.ConnectivityObserver
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

class MovieViewModel(
    private val repository: MovieRepository,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    private val token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI5YzgzOGIwYmI1ZjJmMjhhZDRmYTM3NzIwMjZjNWZkOSIsIm5iZiI6MTc2NzYyOTQ1OS43NjYsInN1YiI6IjY5NWJlMjkzZWM4NmQyZDY4ZjE3YmI0NyIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.j0chOgmWycwc9o23qJFXuzrkIoPyfVGZLPNra0-i6Us"

    // 1. DECLARE ALL STATE FLOWS FIRST
    private val _isOffline = MutableStateFlow(false)
    val isOffline = _isOffline.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val _popularMovies = MutableStateFlow<List<Movie>>(emptyList())
    val popularMovies = _popularMovies.asStateFlow()

    private val _upcomingMovies = MutableStateFlow<List<Movie>>(emptyList())
    val upcomingMovies = _upcomingMovies.asStateFlow()

    private val _topRatedMovies = MutableStateFlow<List<Movie>>(emptyList())
    val topRatedMovies = _topRatedMovies.asStateFlow()

    private val _nowPlayingMovies = MutableStateFlow<List<Movie>>(emptyList())
    val nowPlayingMovies = _nowPlayingMovies.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Movie>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    val watchlist = repository.watchlistFlow()
    val watchedList = repository.watchedFlow()

    // 2. INIT BLOCK COMES AFTER PROPERTIES
    init {
        observeNetwork()
        refreshAll()
    }

    private fun observeNetwork() {
        connectivityObserver.observe()
            .onEach { status ->
                when (status) {
                    ConnectivityObserver.Status.Lost -> {
                        _isOffline.value = true
                        _errorMessage.value = "You are currently offline. Showing local data."
                    }
                    ConnectivityObserver.Status.Available -> {
                        if (_isOffline.value) {
                            _errorMessage.value = "Connection restored!"
                        }
                        _isOffline.value = false
                        refreshAll()
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun clearError() { _errorMessage.value = null }

    fun refreshAll() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                _errorMessage.value = null

                // Fetch Categories concurrently
                val jobs = listOf(
                    launch { fetchPopularMovies() },
                    launch { fetchUpcomingMovies() },
                    launch { fetchTopRatedMovies() },
                    launch { fetchNowPlayingMovies() }
                )
                jobs.joinAll()
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private fun handleError(e: Exception) {
        Log.e("TMDB", "Network error", e)
        if (_errorMessage.value == null) {
            _errorMessage.value = if (e is java.io.IOException) {
                "No internet connection. Please check your network."
            } else {
                "Unable to fetch movies. Please try again later."
            }
        }
    }

    // --- API FETCH FUNCTIONS ---
    fun fetchPopularMovies() { viewModelScope.launch { try { _popularMovies.value = repository.fetchPopularMovies(token).results } catch (e: Exception) { handleError(e) } } }
    fun fetchUpcomingMovies() { viewModelScope.launch { try { _upcomingMovies.value = repository.fetchUpcomingMovies(token).results } catch (e: Exception) { handleError(e) } } }
    fun fetchTopRatedMovies() { viewModelScope.launch { try { _topRatedMovies.value = repository.fetchTopRatedMovies(token).results } catch (e: Exception) { handleError(e) } } }
    fun fetchNowPlayingMovies() { viewModelScope.launch { try { _nowPlayingMovies.value = repository.fetchNowPlayingMovies(token).results } catch (e: Exception) { handleError(e) } } }

    fun onQueryChange(newValue: String) {
        _query.value = newValue
        val trimmed = newValue.trim()
        if (trimmed.isEmpty()) {
            _searchResults.value = emptyList()
        } else if (trimmed.length >= 2) {
            searchMovies(trimmed)
        }
    }

    private fun searchMovies(q: String) {
        viewModelScope.launch {
            try {
                val results = repository.searchMoviesFiltered(token, q)
                _searchResults.value = results
                Log.d("TMDB", "Search '$q': ${results.size} (filtered)")
            } catch (e: Exception) {
                _searchResults.value = emptyList()
                handleError(e)
            }
        }
    }


    // --- STATS & DETAILS (Restored) ---
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth = _selectedMonth.asStateFlow()

    fun nextMonth() {
        _selectedMonth.value = _selectedMonth.value.plusMonths(1)
    }

    fun prevMonth() {
        _selectedMonth.value = _selectedMonth.value.minusMonths(1)
    }

    fun jumpToToday() {
        _selectedMonth.value = YearMonth.now()
    }

    fun selectYear(year: Int) {
        _selectedMonth.value = YearMonth.of(year, _selectedMonth.value.monthValue)
    }

    fun getYearRange(): List<Int> {
        val startYear = 2010
        val currentYear = LocalDate.now().year
        return (startYear..currentYear).toList().reversed()
    }

    // Inside MovieViewModel.kt

    val statsState: StateFlow<StatsUiState> = combine(
        repository.watchedFlow(),
        _selectedMonth
    ) { watched, currentMonth ->
        val allWatched = watched.filter { it.isWatched && it.watchedAt != null }

        // Filter specifically for the selected month in the archive
        val moviesInMonth = allWatched.filter {
            val date = Instant.ofEpochMilli(it.watchedAt!!).atZone(ZoneId.systemDefault()).toLocalDate()
            YearMonth.from(date) == currentMonth
        }

        StatsUiState(
            watchedCount = allWatched.size, // Total for top cards
            totalMinutes = allWatched.sumOf { it.runtimeMinutes ?: 0 }, // Total for top cards
            monthLabel = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${currentMonth.year}",
            heatmap = buildMonthHeatmap(currentMonth, allWatched.mapNotNull { it.watchedAt }),
            favorites = allWatched.filter { (it.userRating ?: 0f) > 0f }.sortedByDescending { it.userRating ?: 0f }.take(10),
            recent = allWatched.sortedByDescending { it.watchedAt ?: 0L }.take(10),
            watchedInMonth = moviesInMonth // Used for side panel and heatmap clicks
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState())
    data class MovieDetailUiState(val isLoading: Boolean = true, val details: MovieDetailsDto? = null, val inWatchlist: Boolean = false, val isWatched: Boolean = false, val userRating: Float? = null)
    private val _detailState = MutableStateFlow(MovieDetailUiState())
    val detailState = _detailState.asStateFlow()

    fun loadMovieDetails(movieId: Int) {
        viewModelScope.launch {
            _detailState.update { it.copy(isLoading = true) }
            try {
                val details = repository.fetchMovieDetails(token, movieId)
                _detailState.update { it.copy(isLoading = false, details = details) }
            } catch (e: Exception) { _detailState.update { it.copy(isLoading = false) } }
        }
        viewModelScope.launch {
            repository.observeMovieById(movieId).collect { entity ->
                _detailState.update { it.copy(inWatchlist = entity?.inWatchlist ?: false, isWatched = entity?.isWatched ?: false, userRating = entity?.userRating) }
            }
        }
    }

    fun addCurrentDetailToWatchlist() {
        val d = _detailState.value.details ?: return
        val currentState = _detailState.value // Get current UI state

        viewModelScope.launch {
            repository.upsert(
                detailsToEntity(d).copy(
                    // Keep the current watched status from the UI state
                    isWatched = currentState.isWatched,
                    userRating = currentState.userRating,
                    // Set watchlist to true
                    inWatchlist = true
                )
            )
        }
    }

    fun markCurrentDetailAsWatched(rating: Float, watchedAt: Long) {
        val d = _detailState.value.details ?: return
        viewModelScope.launch {
            repository.upsert(
                detailsToEntity(d).copy(
                    inWatchlist = false, // Usually removed from watchlist once watched
                    isWatched = true,
                    userRating = rating,
                    watchedAt = watchedAt
                )
            )
        }
    }

    private fun detailsToEntity(d: MovieDetailsDto) = Movie(
        id = d.id,
        title = d.title,
        overview = d.overview,
        posterPath = d.posterPath,
        releaseDate = d.releaseDate,
        inWatchlist = false,
        isWatched = false,
        runtimeMinutes = d.runtime,
        voteAverage = d.voteAverage
    )
    fun deleteMovie(movieId: Int) { viewModelScope.launch { repository.delete(movieId) } }

    private fun buildMonthHeatmap(yearMonth: YearMonth, watchedAtMillis: List<Long>): List<Int> {
        // 1. Get the actual number of days in this specific month
        val daysInMonth = yearMonth.lengthOfMonth()
        val dayCounts = MutableList(daysInMonth) { 0 }

        watchedAtMillis.forEach { ms ->
            val date = Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()).toLocalDate()
            // 2. Only count if it's within the current year and month
            if (date.year == yearMonth.year && date.month == yearMonth.month) {
                // dayOfMonth is 1-indexed, so subtract 1 for the 0-indexed list
                val dayIdx = date.dayOfMonth - 1
                if (dayIdx in dayCounts.indices) {
                    dayCounts[dayIdx]++
                }
            }
        }
        return dayCounts
    }

    // --- CUSTOM LISTS (Restored) ---
    val customLists = repository.getAllCustomLists().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val customCollections = repository.getCustomLists().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    fun removeMovieFromList(lId: Long, mId: Int) { viewModelScope.launch { repository.removeMovieFromList(lId, mId) } }
    fun deleteCustomList(lId: Long) { viewModelScope.launch { repository.deleteCustomList(lId) } }
    suspend fun createCustomList(name: String) = repository.createCustomList(name)
    suspend fun addMovieToExistingList(mId: Int, lId: Long) = repository.addMovieToCustomList(mId, lId)
    suspend fun createListAndAddMovie(n: String, mId: Int) = repository.createCustomList(n).also { repository.addMovieToCustomList(mId, it) }
    fun getMoviesForList(lId: Long) = repository.getMoviesByListId(lId)
}