package com.example.movilog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModelProvider
import com.example.movilog.data.local.AppDatabase
import com.example.movilog.data.remote.TmdbApiService
import com.example.movilog.data.repository.MovieRepository
import com.example.movilog.navigation.AppNavHost
import com.example.movilog.ui.MovieViewModel
import com.example.movilog.ui.MovieViewModelFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {

        // ✅ Window background BEFORE super (hilft gegen Flash)
        window.setBackgroundDrawableResource(android.R.color.black)

        super.onCreate(savedInstanceState)

        // ✅ System bars dunkel + keine "light" Icons
        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.statusBarColor = android.graphics.Color.BLACK
        window.navigationBarColor = android.graphics.Color.BLACK

        WindowInsetsControllerCompat(window, window.decorView).apply {
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }

        // --- DB + Repository ---
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
            // ✅ Root background (falls NavHost/Screens kurz "transparent" sind)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0B2A36))
            ) {
                AppNavHost(viewModel = viewModel)
            }
        }
    }
}
