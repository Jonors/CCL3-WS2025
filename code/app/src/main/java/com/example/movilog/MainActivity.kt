package com.example.movilog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.ViewModelProvider
import com.example.movilog.data.repository.MovieRepository
import com.example.movilog.data.local.AppDatabase
import com.example.movilog.data.remote.TmdbApiService
import com.example.movilog.navigation.AppNavHost
import com.example.movilog.ui.viewmodel.MovieViewModel
import com.example.movilog.ui.MovieViewModelFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(applicationContext)
        val dao = db.movieDao()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(TmdbApiService::class.java)

        val repository = MovieRepository(dao, apiService)
        val viewModelFactory = MovieViewModelFactory(repository)

        val viewModel = ViewModelProvider(this, viewModelFactory)[MovieViewModel::class.java]

        setContent {
            AppNavHost(viewModel = viewModel)        }

    }
}
