package com.example.movilog.data.remote


import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApiService {

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Header("Authorization") token: String,
        @Header("accept") accept: String = "application/json"
    ): TmdbResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Header("Authorization") token: String,
        @Query("query") query: String,
        @Header("accept") accept: String = "application/json"
    ): TmdbResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Header("Authorization") token: String,
        @Path("movie_id") movieId: Int,
        @Header("accept") accept: String = "application/json"
    ): MovieDetailsDto

}

data class TmdbResponse(
    val results: List<com.example.movilog.data.model.Movie>
)
