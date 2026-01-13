package com.example.movilog.data


import com.example.movilog.data.local.MovieDao
import com.example.movilog.data.remote.TmdbApiService

class MovieRepository(
    private val movieDao: MovieDao,
    private val apiService: TmdbApiService
) {
    suspend fun fetchPopularMovies(token: String) =
        apiService.getPopularMovies(token)

    suspend fun fetchUpcomingMovies(token: String) =
        apiService.getUpcomingMovies(token)

    suspend fun searchMovies(token: String, query: String) =
        apiService.searchMovies(token, query)

    suspend fun fetchMovieDetails(token: String, movieId: Int) =
        apiService.getMovieDetails(token, movieId)

}
