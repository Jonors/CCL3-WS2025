package com.example.movilog.data.model
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class ListWithMovies(
    @Embedded val customList: CustomList,
    @Relation(
        parentColumn = "listId",    // Column in CustomList
        entityColumn = "id",        // Primary Key in Movie
        associateBy = Junction(
            value = MovieListCrossRef::class,
            parentColumn = "listId",
            entityColumn = "movieId"
        )
    )
    val movies: List<Movie>
)