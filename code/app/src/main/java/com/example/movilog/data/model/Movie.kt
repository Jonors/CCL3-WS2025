package com.example.movilog.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// This tells Room: "Create a table named 'movies' in my phone's memory"
@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey val id: Int,
    val title: String,
    val overview: String,
    @SerializedName("poster_path")
    val posterPath: String?,
    val releaseDate: String?,
    var inWatchlist: Boolean = false,
    var isWatched: Boolean = false,
    var userRating: Float? = null,        // Pflicht sobald watched
    var watchedAt: Long? = null,
    var runtimeMinutes: Int? = null,
    val voteAverage: Float? = null
)
