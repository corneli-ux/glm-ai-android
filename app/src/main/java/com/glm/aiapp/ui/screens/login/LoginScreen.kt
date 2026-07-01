package com.glm.aiapp.ui.screens.login

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginScreen(vm: LoginViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var showEmailForm by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.data != null) {
            try {
                val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    .getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    vm.loginWithGoogle(idToken)
                } else {
                    vm.setError("Google returned no ID token.")
                }
            } catch (e: ApiException) {
                vm.setError("Sign-in error (code ${e.statusCode})")
            }
        }
    }

    fun launchGoogleSignIn() {
        val webClientId = "714767483567-jgo1ls597k3gqs7pcu97j941rndrs9bh.apps.googleusercontent.com"
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId).requestEmail().build()
        val client = GoogleSignIn.getClient(context, gso)
        client.signOut().addOnCompleteListener { googleSignInLauncher.launch(client.signInIntent) }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Column(
            Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Box(
                Modifier.size(56.dp).clip(RoundedCornerShape(16.dp)).background(Color.White),
                contentAlignment = Alignment.Center
            ) { Text("P1", color = Color.Black, fontSize = 18.sp, fontWeight = FontWeight.Bold) }

            Spacer(Modifier.height(20.dp))
            Text("Pullarao 1", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Build apps with AI", color = Color(0xFF666666), fontSize = 14.sp)
            Spacer(Modifier.height(40.dp))

            AnimatedContent(targetState = showEmailForm, label = "form") { showEmail ->
                if (!showEmail) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Google button
                        OutlinedButton(
                            onClick = { launchGoogleSignIn() },
                            enabled = !state.isSubmitting,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White, contentColor = Color.Black),
                            border = BorderStroke(0.dp, Color.Transparent)
                        ) {
                            Text("G", color = Color(0xFF4285F4), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Text("Continue with Google", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                        Spacer(Modifier.height(12.dp))
                        // Email button
                        OutlinedButton(
                            onClick = { showEmailForm = true },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent, contentColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFF333333))
                        ) {
                            Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Continue with email", fontSize = 14.sp)
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        OutlinedTextField(
                            value = state.email, onValueChange = vm::setEmail,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp), label = { Text("Email", color = Color(0xFF666666)) },
                            singleLine = true,
                            colors = fieldColors()
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = state.password, onValueChange = vm::setPassword,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp), label = { Text("Password", color = Color(0xFF666666)) },
                            singleLine = true,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = { IconButton(onClick = { showPassword = !showPassword }) { Icon(if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = null, tint = Color(0xFF666666)) } },
                            colors = fieldColors()
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = vm::loginWithEmail,
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                            enabled = !state.isSubmitting && state.email.isNotBlank() && state.password.isNotBlank()
                        ) {
                            if (state.isSubmitting) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.Black)
                            else Text("Sign in", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { showEmailForm = false }) { Text("← Back", color = Color(0xFF666666), fontSize = 13.sp) }
                    }
                }
            }

            state.error?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Text(msg, color = Color(0xFFFF6666), fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 16.dp))
            }
            if (state.isSubmitting) {
                Spacer(Modifier.height(12.dp))
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
            }
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
    focusedBorderColor = Color(0xFF444444), unfocusedBorderColor = Color(0xFF222222),
    focusedLabelColor = Color(0xFF888888), unfocusedLabelColor = Color(0xFF555555),
    cursorColor = Color.White,
    focusedContainerColor = Color(0xFF111111), unfocusedContainerColor = Color(0xFF111111)
)
