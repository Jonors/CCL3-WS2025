package com.example.movilog.data.repository

import com.example.movilog.data.local.MovieDao
import com.example.movilog.data.model.Movie
import com.example.movilog.data.remote.TmdbApiService

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

    fun observeMovieById(movieId: Int) = movieDao.observeMovieById(movieId)

}

