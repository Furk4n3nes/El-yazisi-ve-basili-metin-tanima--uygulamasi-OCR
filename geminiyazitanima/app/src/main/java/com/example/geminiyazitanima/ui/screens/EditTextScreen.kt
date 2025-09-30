package com.example.geminiyazitanima.ui.screens

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.itextpdf.text.Document
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfWriter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun EditTextScreen(
    initialText: String,
    onSave: (String) -> Unit,
    onBack: () -> Unit,
    databaseHelper: com.example.geminiyazitanima.data.DatabaseHelper,
    userName: String
) {
    var editedText by remember { mutableStateOf(initialText) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var fileName by remember { mutableStateOf("") }
    val context = LocalContext.current
    val currentDate = remember {
        SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F8F8))) { // Arka plan rengi değiştirildi
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp), // Yan boşluklar eklendi
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Metni Düzenle", // Başlık güncellendi
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D2536),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Metin düzenleme alanı
            OutlinedTextField(
                value = editedText,
                onValueChange = { editedText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = { Text("Metni Düzenle") }
            )
        }

        if (showSaveDialog) {
            Dialog(onDismissRequest = { showSaveDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Belgeyi Kaydet",
                            style = MaterialTheme.typography.titleLarge
                        )

                        OutlinedTextField(
                            value = fileName,
                            onValueChange = { fileName = it },
                            label = { Text("Dosya Adı") },
                            modifier = Modifier.fillMaxWidth(),
                            isError = fileName.isBlank(),
                            supportingText = {
                                if (fileName.isBlank()) {
                                    Text("Dosya adı boş olamaz")
                                }
                            }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    if (fileName.isNotBlank()) {
                                        saveAsTxt(context, fileName, editedText)
                                        // Geçmişe kaydet
                                        val now = Date()
                                        val date = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(now)
                                        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
                                        databaseHelper.addHistory(fileName, date, time, "TXT", userName)
                                        showSaveDialog = false
                                        onSave(editedText)
                                    }
                                },
                                enabled = fileName.isNotBlank(),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("TXT")
                            }

                            FilledTonalButton(
                                onClick = {
                                    if (fileName.isNotBlank()) {
                                        saveAsPdf(context, fileName, editedText)
                                        // Geçmişe kaydet
                                        val now = Date()
                                        val date = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(now)
                                        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
                                        databaseHelper.addHistory(fileName, date, time, "PDF", userName)
                                        showSaveDialog = false
                                        onSave(editedText)
                                    }
                                },
                                enabled = fileName.isNotBlank(),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("PDF")
                            }
                        }

                        TextButton(
                            onClick = { showSaveDialog = false }
                        ) {
                            Text("İptal")
                        }
                    }
                }
            }
        }

        // Geri butonu (sol altta) - Rengi silikleştirildi ve konumu ayarlandı
        Box(modifier = Modifier.fillMaxSize().padding(bottom = 8.dp, start = 8.dp), contentAlignment = Alignment.BottomStart) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", modifier = Modifier.size(48.dp), tint = Color.Gray)
            }
        }

        // Kaydet butonu (sağ altta)
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomEnd) {
             FloatingActionButton(
                onClick = { 
                    fileName = currentDate
                    showSaveDialog = true 
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Save, contentDescription = "Kaydet")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEditTextScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = com.example.geminiyazitanima.data.DatabaseHelper(context)
    EditTextScreen(
        initialText = "Önizleme metni buraya gelecek.",
        onSave = { /* Yapılacak bir şey yok */ },
        onBack = { /* Yapılacak bir şey yok */ },
        databaseHelper = db,
        userName = "TestUser"
    )
}

private fun saveAsTxt(context: Context, fileName: String, content: String) {
    try {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val appDir = File(downloadsDir, "El Yazısı Tanıma Belgeleri")
        
        // Klasör yoksa oluştur
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        
        val file = File(appDir, "$fileName.txt")
        
        FileOutputStream(file).use { fos ->
            fos.write(content.toByteArray())
        }
        
        Toast.makeText(
            context,
            "Belge başarıyla kaydedildi: ${file.absolutePath}",
            Toast.LENGTH_LONG
        ).show()
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "Belge kaydedilirken hata oluştu: ${e.localizedMessage}",
            Toast.LENGTH_LONG
        ).show()
    }
}

private fun saveAsPdf(context: Context, fileName: String, content: String) {
    try {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val appDir = File(downloadsDir, "El Yazısı Tanıma Belgeleri")
        
        // Klasör yoksa oluştur
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        
        val file = File(appDir, "$fileName.pdf")
        
        val document = Document()
        PdfWriter.getInstance(document, FileOutputStream(file))
        document.open()
        document.add(Paragraph(content))
        document.close()
        
        Toast.makeText(
            context,
            "PDF başarıyla kaydedildi: ${file.absolutePath}",
            Toast.LENGTH_LONG
        ).show()
    } catch (e: Exception) {
        Toast.makeText(
            context,
            "PDF kaydedilirken hata oluştu: ${e.localizedMessage}",
            Toast.LENGTH_LONG
        ).show()
    }
} 