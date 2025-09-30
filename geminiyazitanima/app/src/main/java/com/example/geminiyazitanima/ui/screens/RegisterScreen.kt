package com.example.geminiyazitanima.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.geminiyazitanima.data.DatabaseHelper
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun RegisterScreen(
    databaseHelper: DatabaseHelper,
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8)) // Arka plan rengi MainScreen ile aynı yapıldı
            .padding(horizontal = 24.dp), // Yan boşluklar eklendi
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Hesap Oluştur",
            fontSize = 32.sp, // Font boyutu MainScreen başlığı ile aynı yapıldı
            fontWeight = FontWeight.Bold, // Font kalınlığı MainScreen başlığı ile aynı yapıldı
            color = Color.Black,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Kullanıcı Adı") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp), // Yuvarlatılmış köşeler eklendi
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Şifre") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp), // Yuvarlatılmış köşeler eklendi
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Şifre Tekrar") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp), // Yuvarlatılmış köşeler eklendi
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        if (showError) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = {
                when {
                    username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                        showError = true
                        errorMessage = "Tüm alanları doldurunuz!"
                    }
                    password != confirmPassword -> {
                        showError = true
                        errorMessage = "Şifreler eşleşmiyor!"
                    }
                    else -> {
                        if (databaseHelper.addUser(username, password)) {
                            onRegisterSuccess()
                        } else {
                            showError = true
                            errorMessage = "Bu kullanıcı adı zaten kullanılıyor!"
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp) // Yükseklik artırıldı
                .padding(bottom = 26.dp), // Boşluk ayarlandı
            shape = RoundedCornerShape(16.dp), // Yuvarlatılmış köşeler eklendi
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)) // Mavi renk kullanıldı
        ) {
            Text("Kayıt Ol", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) // Yazı boyutu ve kalınlığı ayarlandı
        }

        TextButton(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Giriş Ekranına Dön", color = Color(0xFF1877F2)) // Mavi renk kullanıldı
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRegisterScreen() {
    // Preview için sadece UI elemanlarını gösteriyoruz, DatabaseHelper gerektirmiyor.
     Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8)) // Arka plan rengi MainScreen ile aynı yapıldı
            .padding(horizontal = 24.dp), // Yan boşluklar eklendi
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Hesap Oluştur",
            fontSize = 32.sp, // Font boyutu MainScreen başlığı ile aynı yapıldı
            fontWeight = FontWeight.Bold, // Font kalınlığı MainScreen başlığı ile aynı yapıldı
            color = Color.Black,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        OutlinedTextField(
            value = "",
            onValueChange = { },
            label = { Text("Kullanıcı Adı") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp), // Yuvarlatılmış köşeler eklendi
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = "",
            onValueChange = { },
            label = { Text("Şifre") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp), // Yuvarlatılmış köşeler eklendi
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = "",
            onValueChange = { },
            label = { Text("Şifre Tekrar") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp), // Yuvarlatılmış köşeler eklendi
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        // Hata mesajı gizli
        // Text(
        //     text = errorMessage,
        //     color = MaterialTheme.colorScheme.error,
        //     modifier = Modifier.padding(bottom = 16.dp)
        // )

        Button(
            onClick = { /* Preview */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp) // Yükseklik artırıldı
                .padding(bottom = 26.dp), // Boşluk ayarlandı
            shape = RoundedCornerShape(16.dp), // Yuvarlatılmış köşeler eklendi
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)) // Mavi renk kullanıldı
        ) {
            Text("Kayıt Ol", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) // Yazı boyutu ve kalınlığı ayarlandı
        }

        TextButton(
            onClick = { /* Preview */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Giriş Ekranına Dön", color = Color(0xFF1877F2)) // Mavi renk kullanıldı
        }
    }
} 