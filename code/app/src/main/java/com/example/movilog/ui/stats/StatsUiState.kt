package com.example.movilog.ui.stats

import com.example.movilog.data.model.Movie

data class StatsUiState(
    val watchedCount: Int = 0,
    val totalMinutes: Int = 0,
    val monthLabel: String = "",
    val heatmap: List<Int> = emptyList(),
    val favorites: List<Movie> = emptyList(),
    val recent: List<Movie> = emptyList(),
    val watchedInMonth: List<Movie> = emptyList() // Added field
)
