package com.example.geminiyazitanima.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import java.io.File
import com.example.geminiyazitanima.data.HistoryItem
import androidx.core.content.FileProvider

@Composable
fun HistoryScreen(
    databaseHelper: com.example.geminiyazitanima.data.DatabaseHelper,
    userName: String,
    onViewClick: (com.example.geminiyazitanima.data.HistoryItem) -> Unit,
    onShareClick: (com.example.geminiyazitanima.data.HistoryItem) -> Unit,
    onDeleteClick: (com.example.geminiyazitanima.data.HistoryItem) -> Unit,
    onBack: () -> Unit
) {
    val items = remember { mutableStateListOf<com.example.geminiyazitanima.data.HistoryItem>() }
    LaunchedEffect(userName) {
        items.clear()
        items.addAll(databaseHelper.getAllHistory(userName))
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
            .padding(8.dp)
    ) {
        // Üst bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Geri", tint = Color.Gray)
            }
            Text(
                text = "Geçmiş Kayıtlar",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        // Liste
        items.forEach { item ->
            HistoryCard(
                item,
                onViewClick,
                onShareClick,
                {
                    onDeleteClick(it)
                    items.clear()
                    items.addAll(databaseHelper.getAllHistory(userName))
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun HistoryCard(
    item: HistoryItem,
    onViewClick: (HistoryItem) -> Unit,
    onShareClick: (HistoryItem) -> Unit,
    onDeleteClick: (HistoryItem) -> Unit
) {
    val context = LocalContext.current
    val showDeleteDialog = remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Sol: Küçük görsel (placeholder)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3EDE3))
            ) {}
            Spacer(modifier = Modifier.width(16.dp))
            // Orta: Başlık ve tarih
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = item.date, fontSize = 16.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = item.time, fontSize = 16.sp, color = Color.Gray)
                }
            }
            // Sağ: Dosya tipi etiketi
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (item.type == "PDF") Color(0xFF1877F2) else Color(0xFFFF9800))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = item.type,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
        // Alt: Butonlar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 72.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    // Görüntüle butonu: Dosyayı aç
                    val file = getFileForHistoryItem(context, item)
                    val uri = try {
                        FileProvider.getUriForFile(
                            context,
                            context.applicationContext.packageName + ".provider",
                            file
                        )
                    } catch (e: Exception) {
                        null
                    }
                    if (uri != null) {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.setDataAndType(uri, if (item.type == "PDF") "application/pdf" else "text/plain")
                        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Dosya açılırken hata: Uygun bir uygulama bulunamadı veya izin verilmedi.", android.widget.Toast.LENGTH_LONG).show()
                        }
                    } else {
                        android.widget.Toast.makeText(context, "Dosya bulunamadı", android.widget.Toast.LENGTH_LONG).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Görüntüle", color = Color.Black) }
            Button(
                onClick = {
                    // Paylaş butonu
                    val file = getFileForHistoryItem(context, item)
                    val uri = try {
                        FileProvider.getUriForFile(
                            context,
                            context.applicationContext.packageName + ".provider",
                            file
                        )
                    } catch (e: Exception) {
                        null
                    }
                    if (uri != null) {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = if (item.type == "PDF") "application/pdf" else "text/plain"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        val chooser = Intent.createChooser(shareIntent, "Belgeyi paylaş")
                        if (shareIntent.resolveActivity(context.packageManager) != null) {
                            context.startActivity(chooser)
                        } else {
                            android.widget.Toast.makeText(context, "Paylaşacak uygulama bulunamadı", android.widget.Toast.LENGTH_LONG).show()
                        }
                    } else {
                        android.widget.Toast.makeText(context, "Dosya bulunamadı", android.widget.Toast.LENGTH_LONG).show()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Paylaş", color = Color.Black) }
            Button(
                onClick = { showDeleteDialog.value = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0)),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Sil", color = Color.Black) }
        }
        if (showDeleteDialog.value) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog.value = false },
                title = { Text("Sil") },
                text = { Text("Bu belgeyi silmek istediğinize emin misiniz?") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteDialog.value = false
                        // Dosyayı cihazdan sil
                        val file = getFileForHistoryItem(context, item)
                        if (file.exists()) {
                            file.delete()
                        }
                        // Veritabanından sil
                        onDeleteClick(item)
                    }) { Text("Evet", color = Color.Red) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog.value = false }) { Text("Hayır") }
                }
            )
        }
    }
}

fun getFileForHistoryItem(context: android.content.Context, item: HistoryItem): File {
    val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
    val appDir = File(downloadsDir, "El Yazısı Tanıma Belgeleri")
    val ext = if (item.type == "PDF") ".pdf" else ".txt"
    return File(appDir, item.title + ext)
}

@Preview(showBackground = true)
@Composable
fun PreviewHistoryScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = com.example.geminiyazitanima.data.DatabaseHelper(context)
    HistoryScreen(
        databaseHelper = db,
        userName = "TestUser",
        onViewClick = {},
        onShareClick = {},
        onDeleteClick = {},
        onBack = {}
    )
} 