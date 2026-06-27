package com.bghitech.momenta.feature.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bghitech.momenta.R
import com.bghitech.momenta.core.design.*

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    var usernameOrEmail by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    MomentaScreen {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(36.dp))

            MomentaFullLogo(markSize = 68)

            Spacer(modifier = Modifier.height(28.dp))

            MomentaCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (state.isLoginMode) {
                            stringResource(R.string.login_to_momenta)
                        } else {
                            stringResource(R.string.create_account)
                        },
                        color = MomentaText,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(22.dp))

                    if (!state.isLoginMode) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text(stringResource(R.string.username)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = MomentaTextFieldColors(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = if (state.isLoginMode) usernameOrEmail else email,
                        onValueChange = { if (state.isLoginMode) usernameOrEmail = it else email = it },
                        label = {
                            Text(
                                if (state.isLoginMode) {
                                    stringResource(R.string.username_or_email)
                                } else {
                                    stringResource(R.string.email)
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = MomentaTextFieldColors(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(stringResource(R.string.password)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = MomentaTextFieldColors(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = MomentaTextSecondary
                                )
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        )
                    )

                    if (state.error != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = state.error!!,
                            color = MomentaError,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    MomentaPrimaryButton(
                        text = stringResource(R.string.continue_action),
                        onClick = {
                            if (state.isLoginMode) {
                                viewModel.login(usernameOrEmail, password, onAuthSuccess)
                            } else {
                                viewModel.register(username, email, password, onAuthSuccess)
                            }
                        },
                        loading = state.isLoading,
                        enabled = password.isNotBlank() && (if (state.isLoginMode) usernameOrEmail.isNotBlank() else (username.isNotBlank() && email.isNotBlank()))
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (state.isLoginMode) "Нет аккаунта? Создать" else "Уже есть аккаунт? Войти",
                color = MomentaGreen,
                fontSize = 14.sp,
                modifier = Modifier.clickable { viewModel.toggleMode() }
            )
        }
    }
}

@Composable
fun MomentaTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MomentaText,
    unfocusedTextColor = MomentaText,
    cursorColor = MomentaGreen,
    focusedBorderColor = MomentaGreen,
    unfocusedBorderColor = MomentaDivider,
    focusedLabelColor = MomentaGreen,
    unfocusedLabelColor = MomentaTextSecondary,
    focusedPlaceholderColor = MomentaTextSecondary,
    unfocusedPlaceholderColor = MomentaTextSecondary
)
