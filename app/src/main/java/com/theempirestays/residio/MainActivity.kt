package com.theempirestays.residio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.theempirestays.residio.data.ResidioRepository
import com.theempirestays.residio.data.User
import com.theempirestays.residio.ui.screens.HomeScreen
import com.theempirestays.residio.ui.screens.LoginScreen
import com.theempirestays.residio.ui.theme.ResidioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ResidioRepository.load(applicationContext)
        setContent {
            ResidioTheme {
                var user by remember { mutableStateOf<User?>(null) }
                val current = user
                if (current == null) {
                    LoginScreen(onLoggedIn = { user = it })
                } else {
                    HomeScreen(user = current, onLogout = { user = null })
                }
            }
        }
    }
}
