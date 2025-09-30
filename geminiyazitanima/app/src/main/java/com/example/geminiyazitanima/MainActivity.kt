package com.example.geminiyazitanima

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.geminiyazitanima.data.DatabaseHelper
import com.example.geminiyazitanima.ui.screens.EditTextScreen
import com.example.geminiyazitanima.ui.screens.OcrScreen
import com.example.geminiyazitanima.ui.screens.LoginScreen
import com.example.geminiyazitanima.ui.screens.RegisterScreen
import com.example.geminiyazitanima.ui.screens.MainScreen
import com.example.geminiyazitanima.ui.screens.ForgotPasswordScreen
import com.example.geminiyazitanima.ui.screens.HistoryScreen
import com.example.geminiyazitanima.ui.theme.GeminiyazitanimaTheme

class MainActivity : ComponentActivity() {
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseHelper = DatabaseHelper(this)

        setContent {
            AppContent(databaseHelper)
        }
    }

    override fun onDestroy() {
        databaseHelper.close()
        super.onDestroy()
    }
}

@Composable
fun AppContent(databaseHelper: DatabaseHelper) {
    GeminiyazitanimaTheme {
        var currentScreen by remember { mutableStateOf("login") }
        var isLoggedIn by remember { mutableStateOf(false) }
        var recognizedText by remember { mutableStateOf("") }
        var userName by remember { mutableStateOf("") }

        when {
            isLoggedIn -> {
                when (currentScreen) {
                    "main" -> {
                        MainScreen(
                            userName = userName,
                            onScanClick = { currentScreen = "ocr" },
                            onLogoutClick = {
                                isLoggedIn = false
                                userName = ""
                                currentScreen = "login"
                            },
                            onSettingsClick = { /* Ayarlar ekranı eklenebilir */ },
                            onHistoryClick = { currentScreen = "history" },
                            onUpdateUserInfo = { newUsername, newPassword ->
                                databaseHelper.updateUserInfo(userName, newUsername, newPassword)
                                userName = newUsername
                            }
                        )
                    }
                    "ocr" -> {
                        OcrScreen(
                            onNavigateToEdit = { text ->
                                recognizedText = text
                                currentScreen = "edit"
                            },
                            onBackClick = { currentScreen = "main" }
                        )
                    }
                    "edit" -> {
                        EditTextScreen(
                            initialText = recognizedText,
                            onSave = { editedText ->
                                recognizedText = editedText
                                currentScreen = "main"
                            },
                            onBack = {
                                currentScreen = "main"
                            },
                            databaseHelper = databaseHelper,
                            userName = userName
                        )
                    }
                    "history" -> {
                        HistoryScreen(
                            databaseHelper = databaseHelper,
                            userName = userName,
                            onViewClick = {},
                            onShareClick = {},
                            onDeleteClick = { item ->
                                databaseHelper.deleteHistory(item.id)
                            },
                            onBack = { currentScreen = "main" }
                        )
                    }
                }
            }
            currentScreen == "login" -> {
                LoginScreen(
                    databaseHelper = databaseHelper,
                    onLoginSuccess = {
                        isLoggedIn = true
                        userName = it
                        currentScreen = "main"
                    },
                    onNavigateToRegister = { currentScreen = "register" },
                    onForgotPassword = { currentScreen = "forgot" }
                )
            }
            currentScreen == "register" -> {
                RegisterScreen(
                    databaseHelper = databaseHelper,
                    onRegisterSuccess = { currentScreen = "login" },
                    onNavigateBack = { currentScreen = "login" }
                )
            }
            currentScreen == "forgot" -> {
                ForgotPasswordScreen(
                    databaseHelper = databaseHelper,
                    onPasswordReset = { currentScreen = "login" },
                    onNavigateBack = { currentScreen = "login" }
                )
            }
        }
    }
}