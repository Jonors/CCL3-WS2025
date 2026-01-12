package com.example.movilog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.example.movilog.data.MovieRepository
import com.example.movilog.data.local.AppDatabase
import com.example.movilog.data.remote.TmdbApiService
import com.example.movilog.ui.HomeScreen
import com.example.movilog.ui.MovieViewModel
import com.example.movilog.ui.MovieViewModelFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(TmdbApiService::class.java)

        // 2. Initialize Room
        val database = AppDatabase.getDatabase(this)
        val dao = database.movieDao()

        // 3. Initialize Repository
        val repository = MovieRepository(dao, apiService)

        // 4. Create the ViewModel using the Factory
        val viewModelFactory = MovieViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[MovieViewModel::class.java]

        // 5. Set the Content to our Compose View
        setContent {
            // Apply your app's theme here if you have one
            HomeScreen(viewModel = viewModel)
        }
    }
}