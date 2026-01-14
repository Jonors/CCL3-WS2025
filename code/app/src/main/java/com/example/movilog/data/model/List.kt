package com.example.movilog.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "custom_lists")
data class CustomList(
    @PrimaryKey(autoGenerate = true) val listId: Long = 0,
    val listName: String
)

@Entity(
    tableName = "movie_list_cross_ref",
    primaryKeys = ["listId", "movieId"],
    indices = [Index("movieId")]
)
data class MovieListCrossRef(
    val listId: Long,
    val movieId: Int
)