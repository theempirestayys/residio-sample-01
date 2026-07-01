package com.theempirestays.residio.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
    var loading by remember { mutableStateOf(false) }
    val org = ResidioRepository.org

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo
            Text("residio.", fontSize = 44.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Text(org.brand.uppercase(), fontSize = 11.sp, letterSpacing = 3.sp,
                color = MaterialTheme.colorScheme.secondary)
            Text("Hospitality Intelligence", fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))

            Spacer(Modifier.height(40.dp))

            // Google Sign-In
            OutlinedButton(
                onClick = {
                    val googleUser = ResidioRepository.authenticateGoogle("master@theempirestayys.com")
                    if (googleUser != null) onLoggedIn(googleUser)
                    else error = "Google account not linked. Use username/password."
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Google, null, Modifier.size(20.dp),
                    tint = Color(0xFFDB4437))
                Spacer(Modifier.width(10.dp))
                Text("Continue with Google", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(16.dp))

            // Biometric button
            OutlinedButton(
                onClick = { /* Biometric prompt would launch here */ },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Fingerprint, null, Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                Text("Use Biometric / Face ID", fontSize = 15.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(Modifier.height(20.dp))

            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(Modifier.weight(1f))
                Text("  or sign in with username  ", fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f))
                HorizontalDivider(Modifier.weight(1f))
            }

            Spacer(Modifier.height(16.dp))

            // Username
            OutlinedTextField(
                value = username,
                onValueChange = { username = it; error = null },
                label = { Text("Username") },
                leadingIcon = { Icon(Icons.Filled.Person, null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; error = null },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Filled.Lock, null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (show) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { show = !show }) {
                        Icon(if (show) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, null)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp,
                    textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = {
                    loading = true
                    val u = ResidioRepository.authenticate(username, password)
                    loading = false
                    if (u != null) onLoggedIn(u) else error = "Invalid username or password"
                },
                enabled = !loading,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (loading) {
                    CircularProgressIndicator(Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.Login, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = {}) {
                Text("Forgot password?", fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary)
            }

            Spacer(Modifier.height(20.dp))

            // Demo credentials
            Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(10.dp)) {
                Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Demo Logins", fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "master / residio@master\nrajiv / owner@1001  ·  anita / owner@1002\nsuresh / staff@2001  ·  priya / staff@2002",
                        fontSize = 10.sp, textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f)
                    )
                }
            }
        }
    }
}
