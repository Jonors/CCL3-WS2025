package com.example.movilog.data

import com.example.movilog.data.local.MovieDao
import com.example.movilog.data.model.Movie
import com.example.movilog.data.remote.TmdbApiService

class MovieRepository(
    private val movieDao: MovieDao,
    private val apiService: TmdbApiService
) {
    // The Manager asks the API for new movies
    suspend fun fetchNewMovies(apiKey: String) = apiService.getPopularMovies(apiKey)

    // The Manager saves a movie to the phone's local storage
    suspend fun saveToWatchlist(movie: Movie) {
        movieDao.insertMovie(movie)
    }

    // The Manager gets the saved list from the phone's memory
    fun getMyWatchlist() = movieDao.getAllMovies()
}