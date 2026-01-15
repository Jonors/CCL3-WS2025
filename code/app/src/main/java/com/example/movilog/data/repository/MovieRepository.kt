package com.example.movilog.data.repository

import com.example.movilog.data.local.MovieDao
import com.example.movilog.data.model.CustomList
import com.example.movilog.data.model.ListWithMovies
import com.example.movilog.data.model.Movie
import com.example.movilog.data.model.MovieListCrossRef
import com.example.movilog.data.remote.TmdbApiService
import kotlinx.coroutines.flow.Flow

class MovieRepository(
    private val movieDao: MovieDao,
    private val apiService: TmdbApiService
) {
    // remote
    suspend fun fetchPopularMovies(token: String) = apiService.getPopularMovies(token)
    suspend fun searchMovies(token: String, query: String) = apiService.searchMovies(token, query)
    suspend fun fetchMovieDetails(token: String, movieId: Int) = apiService.getMovieDetails(token, movieId)

    // local
    fun watchlistFlow() = movieDao.getWatchlist()
    fun watchedFlow() = movieDao.getWatchedList()
    suspend fun fetchTopRatedMovies(token: String) =
        apiService.getTopRatedMovies(token)

    suspend fun upsert(movie: Movie) = movieDao.insertMovie(movie)
    suspend fun addToWatchlist(movieId: Int) = movieDao.setInWatchlist(movieId)
    suspend fun markAsWatched(movieId: Int, rating: Float, watchedAt: Long) = movieDao.setWatched(movieId, rating, watchedAt)
    suspend fun delete(movieId: Int) = movieDao.deleteMovie(movieId)
    suspend fun getMovieById(movieId: Int) = movieDao.getMovieById(movieId)

    suspend fun fetchNowPlayingMovies(token: String) =
        apiService.getNowPlayingMovies(token)

    suspend fun fetchUpcomingMovies(token: String) =
        apiService.getUpcomingMovies(token)

    // Fetch all lists (simple list for the selection dialog)
    fun getAllCustomLists(): Flow<List<CustomList>> = movieDao.getAllCustomLists()

    fun getCustomLists() = movieDao.getAllListsWithMovies()

    // Add movie to a specific list
    suspend fun addMovieToCustomList(movieId: Int, listId: Long) {
        movieDao.addMovieToList(MovieListCrossRef(listId = listId, movieId = movieId))
    }

    // Create a new list (e.g., "Old but Gold")
    suspend fun createCustomList(name: String): Long {
        return movieDao.createCustomList(CustomList(listName = name))
    }

    fun observeMovieById(movieId: Int) = movieDao.observeMovieById(movieId)

    suspend fun removeMovieFromList(listId: Long, movieId: Int) = movieDao.removeMovieFromList(listId, movieId)

    suspend fun deleteCustomList(listId: Long) = movieDao.deleteFullCustomList(listId)

    fun getMoviesByListId(listId: Long): Flow<ListWithMovies> {
        return movieDao.getMoviesByListId(listId)
    }
}

