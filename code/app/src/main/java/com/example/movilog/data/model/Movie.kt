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
    @SerializedName("poster_path") val posterPath: String?, // TMDB uses snake_case
    @SerializedName("release_date") val releaseDate: String?,
    var userRating: Float = 0f,
    var isWatched: Boolean = false
)