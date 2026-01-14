package com.example.movilog.data.local

import androidx.room.*
import com.example.movilog.data.model.CustomList
import com.example.movilog.data.model.ListWithMovies
import com.example.movilog.data.model.Movie
import com.example.movilog.data.model.MovieListCrossRef
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    // Add a movie to our watchlist
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: Movie)

    @Query("SELECT * FROM movies WHERE inWatchlist = 1 AND isWatched = 0 ORDER BY id DESC")
    fun getWatchlist(): Flow<List<Movie>>

    @Query("SELECT * FROM movies WHERE isWatched = 1 ORDER BY watchedAt DESC")
    fun getWatchedList(): Flow<List<Movie>>

    // Get all movies we saved
    @Query("SELECT * FROM movies")
    fun getAllMovies(): kotlinx.coroutines.flow.Flow<List<Movie>>

    // Change the rating of a movie
    @Update
    suspend fun updateMovie(movie: Movie)

    // Remove a movie from the list
    @Query("UPDATE movies SET inWatchlist = 1 WHERE id = :movieId")
    suspend fun setInWatchlist(movieId: Int)

    @Query("UPDATE movies SET isWatched = 1, userRating = :rating, watchedAt = :watchedAt, inWatchlist = 0 WHERE id = :movieId")
    suspend fun setWatched(movieId: Int, rating: Float, watchedAt: Long)

    @Query("DELETE FROM movies WHERE id = :movieId")
    suspend fun deleteMovie(movieId: Int)

    @Query("UPDATE movies SET inWatchlist = 1 WHERE id = :movieId")
    suspend fun addToWatchlist(movieId: Int)

    @Query("SELECT * FROM movies WHERE id = :movieId LIMIT 1")
    suspend fun getMovieById(movieId: Int): Movie?

    @Query("SELECT * FROM movies WHERE id = :movieId LIMIT 1")
    fun observeMovieById(movieId: Int): kotlinx.coroutines.flow.Flow<Movie?>

    // --- Custom List Methods ---

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun createCustomList(customList: CustomList): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addMovieToList(crossRef: MovieListCrossRef)

    @Transaction // Important: Room does two queries here, so it must be a transaction
    @Query("SELECT * FROM custom_lists")
    fun getAllListsWithMovies(): Flow<List<ListWithMovies>>

    @Transaction
    @Query("SELECT * FROM custom_lists WHERE listId = :listId")
    fun getMoviesByListId(listId: Long): Flow<ListWithMovies>

    @Query("SELECT * FROM custom_lists")
    fun getAllCustomLists(): Flow<List<CustomList>>

    // Remove a specific movie from a specific custom list
    @Query("DELETE FROM movie_list_cross_ref WHERE listId = :listId AND movieId = :movieId")
    suspend fun removeMovieFromList(listId: Long, movieId: Int)

    // Delete the list and all its movie associations
    @Transaction
    suspend fun deleteFullCustomList(listId: Long) {
        deleteMovieAssociationsForList(listId)
        deleteListEntity(listId)
    }

    @Query("DELETE FROM movie_list_cross_ref WHERE listId = :listId")
    suspend fun deleteMovieAssociationsForList(listId: Long)

    @Query("DELETE FROM custom_lists WHERE listId = :listId")
    suspend fun deleteListEntity(listId: Long)
}
