package com.example.movilog.data.local

import androidx.room.*
import com.example.movilog.data.model.Movie

@Dao
interface MovieDao {
    // Add a movie to our watchlist
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: Movie)

    // Get all movies we saved
    @Query("SELECT * FROM movies")
    fun getAllMovies(): List<Movie>

    // Change the rating of a movie
    @Update
    suspend fun updateMovie(movie: Movie)

    // Remove a movie from the list
    @Delete
    suspend fun deleteMovie(movie: Movie)
}