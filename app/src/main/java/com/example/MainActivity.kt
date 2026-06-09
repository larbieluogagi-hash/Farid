package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.PregnancyDatabase
import com.example.data.PregnancyRepository
import com.example.ui.MainContainer
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PregnancyViewModel
import com.example.viewmodel.PregnancyViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Edge to edge full system bleed support
        enableEdgeToEdge()

        // Core Database and Repository initialization
        val database = PregnancyDatabase.getDatabase(applicationContext)
        val repository = PregnancyRepository(database)

        // ViewModel compilation
        val factory = PregnancyViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, factory)[PregnancyViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainContainer(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
