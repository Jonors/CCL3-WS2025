package com.example.movilog.data.remote


import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Response
import retrofit2.http.Header

interface TmdbApiService {
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Header("Authorization") token: String,
        @Header("accept") accept: String = "application/json"
    ): TmdbResponse
}

// A simple box to hold the list of movies TMDB sends back
data class TmdbResponse(
    val results: List<com.example.movilog.data.model.Movie>
)