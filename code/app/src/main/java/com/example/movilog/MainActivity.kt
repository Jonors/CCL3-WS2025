package com.example.movilog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import com.example.movilog.data.local.AppDatabase
import com.example.movilog.data.remote.TmdbApiService
import com.example.movilog.data.repository.MovieRepository
import com.example.movilog.navigation.AppNavHost
import com.example.movilog.ui.MovieViewModel
import com.example.movilog.ui.MovieViewModelFactory
import com.example.movilog.util.ConnectivityObserver
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Edge-to-edge setup
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        // Standard DI / ViewModel Setup
        val db = AppDatabase.getDatabase(applicationContext)
        val dao = db.movieDao()
        val connectivityObserver = ConnectivityObserver(applicationContext)
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.themoviedb.org/3/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(TmdbApiService::class.java)
        val repository = MovieRepository(dao, apiService)
        val viewModelFactory = MovieViewModelFactory(repository, connectivityObserver)
        val viewModel = ViewModelProvider(this, viewModelFactory)[MovieViewModel::class.java]

        setContent {
            AppNavHost(viewModel = viewModel)
        }
    }
}