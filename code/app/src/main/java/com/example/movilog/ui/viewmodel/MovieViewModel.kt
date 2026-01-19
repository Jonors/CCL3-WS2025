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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale




class MovieViewModel(private val repository: MovieRepository) : ViewModel() {

    // ✅ Hier dein TMDB v4 Read Access Token rein:
    private val token = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI5YzgzOGIwYmI1ZjJmMjhhZDRmYTM3NzIwMjZjNWZkOSIsIm5iZiI6MTc2NzYyOTQ1OS43NjYsInN1YiI6IjY5NWJlMjkzZWM4NmQyZDY4ZjE3YmI0NyIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.j0chOgmWycwc9o23qJFXuzrkIoPyfVGZLPNra0-i6Us"

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _popularMovies = MutableStateFlow<List<Movie>>(emptyList())
    val popularMovies: StateFlow<List<Movie>> = _popularMovies.asStateFlow()

    private val _upcomingMovies = MutableStateFlow<List<Movie>>(emptyList())

    val upcomingMovies: StateFlow<List<Movie>> = _upcomingMovies.asStateFlow()

    private val _topRatedMovies = MutableStateFlow<List<Movie>>(emptyList())

    val topRatedMovies: StateFlow<List<Movie>> = _topRatedMovies.asStateFlow()

    private val _nowPlayingMovies = MutableStateFlow<List<Movie>>(emptyList())

    val nowPlayingMovies: StateFlow<List<Movie>> = _nowPlayingMovies.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Movie>>(emptyList())
    val searchResults: StateFlow<List<Movie>> = _searchResults.asStateFlow()

    val watchlist = repository.watchlistFlow()
    val watchedList = repository.watchedFlow()


    init {
        fetchPopularMovies()
        fetchUpcomingMovies()
        fetchTopRatedMovies()
        fetchNowPlayingMovies()
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

    fun fetchTopRatedMovies() {
        viewModelScope.launch {
            try {
                val response = repository.fetchTopRatedMovies(token)
                _topRatedMovies.value = response.results
                Log.d("TMDB", "Upcoming loaded: ${response.results.size}")
            } catch (e: Exception) {
                _topRatedMovies.value = emptyList()
                Log.e("TMDB", "Failed to load top-rated movies", e)
            }
        }
    }

    fun fetchNowPlayingMovies() {
        viewModelScope.launch {
            try {
                val response = repository.fetchNowPlayingMovies(token)
                _nowPlayingMovies.value = response.results
                Log.d("TMDB", "Upcoming loaded: ${response.results.size}")
            } catch (e: Exception) {
                _nowPlayingMovies.value = emptyList()
                Log.e("TMDB", "Failed to load now-playing movies", e)
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
                val results = repository.searchMoviesFiltered(token, q)
                _searchResults.value = results
                Log.d("TMDB", "Search '$q': ${results.size} (filtered)")
            } catch (e: Exception) {
                _searchResults.value = emptyList()
                Log.e("TMDB", "Failed to search movies", e)
            }
        }
    }


    data class MovieDetailUiState(
        val isLoading: Boolean = true,
        val details: MovieDetailsDto? = null,
        val inWatchlist: Boolean = false,
        val isWatched: Boolean = false,
        val userRating: Float? = null
    )

    val statsState: StateFlow<StatsUiState> =
        repository.watchedFlow()
            .map { watched ->
                val watchedOnly = watched.filter { it.isWatched && it.watchedAt != null }

                val watchedCount = watchedOnly.size
                val totalMinutes = watchedOnly.sumOf { it.runtimeMinutes ?: 0 }

                val now = LocalDate.now()
                val yearMonth = YearMonth.from(now)
                val monthLabel =
                    "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)} ${yearMonth.year}"

                val heatmap = buildMonthHeatmap(
                    yearMonth,
                    watchedOnly.mapNotNull { it.watchedAt }
                )

                val favorites = watchedOnly
                    .filter { (it.userRating ?: 0f) > 0f }
                    .sortedByDescending { it.userRating ?: 0f }
                    .take(10)

                val recent = watchedOnly
                    .sortedByDescending { it.watchedAt ?: 0L }
                    .take(10)

                StatsUiState(
                    watchedCount = watchedCount,
                    totalMinutes = totalMinutes,
                    monthLabel = monthLabel,
                    heatmap = heatmap,
                    favorites = favorites,
                    recent = recent
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsUiState())


    private val _detailState = MutableStateFlow(MovieDetailUiState())
    val detailState: StateFlow<MovieDetailUiState> = _detailState.asStateFlow()

    private var detailDbJob: kotlinx.coroutines.Job? = null

    fun loadMovieDetails(movieId: Int) {
        // 1) DB observer starten (und alten stoppen)
        detailDbJob?.cancel()
        detailDbJob = viewModelScope.launch {
            repository.observeMovieById(movieId).collect { entity ->
                _detailState.value = _detailState.value.copy(
                    inWatchlist = entity?.inWatchlist ?: false,
                    isWatched = entity?.isWatched ?: false,
                    userRating = entity?.userRating
                )
            }
        }

        // 2) TMDB Details laden (Film-Infos)
        viewModelScope.launch {
            _detailState.value = _detailState.value.copy(isLoading = true)
            try {
                val details = repository.fetchMovieDetails(token, movieId)
                _detailState.value = _detailState.value.copy(
                    isLoading = false,
                    details = details
                )
            } catch (e: Exception) {
                _detailState.value = _detailState.value.copy(isLoading = false, details = null)
            }
        }
    }


    fun addMovieToWatchlist(movieId: Int) {
        val d = _detailState.value.details ?: return
        viewModelScope.launch {
            // ✅ ensures row exists
            repository.upsert(detailsToEntity(d).copy(inWatchlist = true))
            // optional: falls du UPDATE behalten willst, aber nicht nötig
            // repository.addToWatchlist(movieId)

            _detailState.value = _detailState.value.copy(inWatchlist = true)
        }
    }

    fun markMovieAsWatched(movieId: Int, rating: Float, watchedAt: Long) {
        val d = _detailState.value.details ?: return
        viewModelScope.launch {
            // ✅ ensures row exists + stores watched fields
            repository.upsert(
                detailsToEntity(d).copy(
                    inWatchlist = false,
                    isWatched = true,
                    userRating = rating,
                    watchedAt = watchedAt
                )
            )
            // optional: falls du UPDATE behalten willst, aber nicht nötig
            // repository.markWatched(movieId, rating, watchedAt)

            _detailState.value = _detailState.value.copy(
                isWatched = true,
                inWatchlist = false,
                userRating = rating
            )
        }
    }


    private fun detailsToEntity(d: MovieDetailsDto) =
        Movie(
            id = d.id,
            title = d.title,
            overview = d.overview,
            posterPath = d.posterPath,
            releaseDate = d.releaseDate,
            inWatchlist = true,
            isWatched = false,
            userRating = null,
            watchedAt = null,
            runtimeMinutes = d.runtime,
            voteAverage = d.voteAverage

        )

    fun addCurrentDetailToWatchlist() {
        val d = _detailState.value.details ?: return
        viewModelScope.launch {
            repository.upsert(detailsToEntity(d))   // wichtig: row exists
            repository.addToWatchlist(d.id)
            _detailState.value = _detailState.value.copy(inWatchlist = true)
        }
    }

    fun markCurrentDetailAsWatched(rating: Float, watchedAt: Long) {
        val d = _detailState.value.details ?: return
        viewModelScope.launch {
            // ensure movie exists in DB
            repository.upsert(
                detailsToEntity(d).copy(
                    inWatchlist = false,
                    isWatched = true,
                    userRating = rating,
                    watchedAt = watchedAt
                )
            )
            repository.markAsWatched(d.id, rating, watchedAt)

            _detailState.value = _detailState.value.copy(
                isWatched = true,
                inWatchlist = false,
                userRating = rating
            )
        }
    }

    fun deleteMovie(movieId: Int) {
        viewModelScope.launch { repository.delete(movieId) }
    }


    private fun buildMonthHeatmap(yearMonth: YearMonth, watchedAtMillis: List<Long>): List<Int> {
        val zone = ZoneId.systemDefault()

        val counts = IntArray(yearMonth.lengthOfMonth() + 1)
        watchedAtMillis.forEach { ms ->
            val date = Instant.ofEpochMilli(ms).atZone(zone).toLocalDate()
            if (date.year == yearMonth.year && date.month == yearMonth.month) {
                counts[date.dayOfMonth] += 1
            }
        }

        val firstDay = yearMonth.atDay(1)
        val firstDowIndex = (firstDay.dayOfWeek.value - 1) // Mon=0
        val totalCells = firstDowIndex + yearMonth.lengthOfMonth()
        val weeks = ((totalCells + 6) / 7).coerceAtLeast(5)

        val grid = MutableList(weeks * 7) { 0 }
        for (day in 1..yearMonth.lengthOfMonth()) {
            val cellIndex = firstDowIndex + (day - 1)
            grid[cellIndex] = counts[day]
        }
        return grid
    }

    // 1. Observe all available custom lists
    val customLists: StateFlow<List<CustomList>> = repository.getAllCustomLists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Function to add movie to a list
    suspend fun addMovieToListAndGetId(movieId: Int, listId: Long): Long {
        repository.addMovieToCustomList(movieId, listId)
        return listId
    }

    // 3. Function to create a new list
    suspend fun createListAndAddMovie(name: String, movieId: Int): Long {
        // 1. Create the list and get the new ID back
        val newListId = repository.createCustomList(name)

        // 2. Associate the movie with this new list
        repository.addMovieToCustomList(movieId, newListId)

        return newListId
    }

    suspend fun addMovieToExistingList(movieId: Int, listId: Long): Long {
        repository.addMovieToCustomList(movieId, listId)
        return listId
    }

    // Observe all custom collections with their associated movies
    val customCollections: StateFlow<List<ListWithMovies>> = repository.getCustomLists()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun removeMovieFromList(listId: Long, movieId: Int) {
        viewModelScope.launch {
            repository.removeMovieFromList(listId, movieId)
        }
    }

    suspend fun createCustomList(name: String):Long{
        val newListId = repository.createCustomList(name)
        return newListId
    }

    fun deleteCustomList(listId: Long) {
        viewModelScope.launch {
            repository.deleteCustomList(listId)
        }
    }

    fun getMoviesForList(listId: Long): Flow<ListWithMovies> {
        return repository.getMoviesByListId(listId)
    }
}

