package com.theempirestays.residio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.theempirestays.residio.data.ResidioRepository
import com.theempirestays.residio.data.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoggedIn: (User) -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var show by remember { mutableStateOf(false) }
    val org = ResidioRepository.org

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("residio.", fontSize = 40.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Text(org.brand.uppercase(), fontSize = 12.sp, letterSpacing = 3.sp,
                color = MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(40.dp))

            OutlinedTextField(
                value = username, onValueChange = { username = it; error = null },
                label = { Text("Username") }, singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password, onValueChange = { password = it; error = null },
                label = { Text("Password") }, singleLine = true,
                visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions.Default,
                trailingIcon = {
                    IconButton(onClick = { show = !show }) {
                        Icon(if (show) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (error != null) {
                Spacer(Modifier.height(10.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    val u = ResidioRepository.authenticate(username, password)
                    if (u != null) onLoggedIn(u) else error = "Invalid username or password"
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Sign in", fontSize = 16.sp) }

            Spacer(Modifier.height(28.dp))
            Text(
                "Demo logins\nmaster / residio@master\nrajiv / owner@1001    ·    anita / owner@1002\nsuresh / staff@2001    ·    priya / staff@2002",
                fontSize = 11.sp, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
            )
        }
    }
}
