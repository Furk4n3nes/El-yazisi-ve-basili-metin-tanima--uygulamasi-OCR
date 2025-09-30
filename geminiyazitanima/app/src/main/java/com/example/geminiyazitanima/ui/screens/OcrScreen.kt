package com.example.geminiyazitanima.ui.screens

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.ui.tooling.preview.Preview

private const val API_KEY = "Your-APİ-KEY"
private const val TAG = "OcrScreen"
private const val MODEL_NAME = "gemini-2.0-flash"

@Composable
fun OcrScreen(
    onNavigateToEdit: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var permissionToRequest by remember { mutableStateOf("") }
    var recognizedText by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var cameraImageUri by remember { mutableStateOf<Uri?>(null) }

    val generativeModel = remember {
        GenerativeModel(
            modelName = MODEL_NAME,
            apiKey = API_KEY,
            generationConfig = generationConfig {
                temperature = 0.4f
                topK = 32
                topP = 1f
                maxOutputTokens = 1024
            }
        )
    }

    // Galeri için launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
        recognizedText = null
    }

    val createNewImageUri: () -> Uri = {
        val fileName = "photo_${System.currentTimeMillis()}.jpg"
        val file = File(context.externalCacheDir, fileName)
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    // Kamera için launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        try {
            if (success && cameraImageUri != null) {
                recognizedText = null
                // Kamera ile çekilen fotoğrafı galeriye kaydet
                try {
                    val bitmap = uriToBitmap(context.contentResolver, cameraImageUri!!)
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, "photo_${System.currentTimeMillis()}.jpg")
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                        }
                    }
                    context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let { uri ->
                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        }
                    }
                    // Galerideki en son fotoğrafı önizleme olarak göster
                    imageUri = getLastImageUri(context)
                } catch (e: Exception) {
                    Log.e(TAG, "Fotoğraf kaydedilirken hata oluştu", e)
                    Toast.makeText(context, "Fotoğraf kaydedilirken hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
                    imageUri = null
                }
            } else {
                imageUri = null
                Toast.makeText(context, "Fotoğraf çekilemedi", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Kamera işlemi sırasında hata oluştu", e)
            Toast.makeText(context, "Kamera işlemi sırasında hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
            imageUri = null
        }
    }

    // İzin launcher'ı
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            when (permissionToRequest) {
                Manifest.permission.CAMERA -> {
                    try {
                        val uri = createNewImageUri()
                        cameraImageUri = uri
                        imageUri = uri
                        cameraLauncher.launch(uri)
                    } catch (e: Exception) {
                        Log.e(TAG, "Kamera başlatılırken hata oluştu", e)
                        Toast.makeText(context, "Kamera başlatılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> galleryLauncher.launch("image/*")
            }
        } else {
            Toast.makeText(context, "Kamera izni gerekli", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F8F8))) {
    Column(
        modifier = Modifier
            .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Belge Tara",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D2536),
                modifier = Modifier.padding(bottom = 32.dp)
            )
            // Kamera ve Galeri Butonları
            Row(
                    modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
        Button(
            onClick = {
                when {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        try {
                            val uri = createNewImageUri()
                            cameraImageUri = uri
                            imageUri = uri
                            cameraLauncher.launch(uri)
                        } catch (e: Exception) {
                            Log.e(TAG, "Kamera başlatılırken hata oluştu", e)
                            Toast.makeText(context, "Kamera başlatılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> {
                        permissionToRequest = Manifest.permission.CAMERA
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }
            },
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
            modifier = Modifier
                .height(64.dp)
                .weight(1f)
        ) {
            Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                Text(
                    "Kamera ile\nTara",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(
            onClick = {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.READ_MEDIA_IMAGES
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                galleryLauncher.launch("image/*")
                            }
                            else -> {
                                permissionToRequest = Manifest.permission.READ_MEDIA_IMAGES
                                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                            }
                        }
                    }
                    else -> {
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                galleryLauncher.launch("image/*")
                            }
                            else -> {
                                permissionToRequest = Manifest.permission.READ_EXTERNAL_STORAGE
                                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                            }
                        }
                    }
                }
            },
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
            modifier = Modifier
                .height(64.dp)
                .weight(1f)
        ) {
            Icon(Icons.Filled.Image, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                Text(
                    "Galeriden\nSeç",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp
                )
            }
        }
            }
            Spacer(modifier = Modifier.height(32.dp))
            // Belge İllüstrasyonu veya Seçilen Resim
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .aspectRatio(1f),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Seçilen resim",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                } else {
                    // Placeholder belge illüstrasyonu
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(32.dp))
                            .background(Color(0xFFE9E1D6)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(0xFFF5EEDD))
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            // Tikli Yuvarlak Buton
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
            Button(
                onClick = {
                        coroutineScope.launch {
                            isLoading = true
                            try {
                                val bitmap = uriToBitmap(context.contentResolver, imageUri!!)
                                val prompt = "Bu resimde gördüğün el yazısını oku ve metni bana ver. Sadece metni ver, başka bir şey yazma."
                                val inputContent = content {
                                    image(bitmap)
                                    text(prompt)
                                }
                                val response = generativeModel.generateContent(inputContent)
                                recognizedText = response.text
                                if (!recognizedText.isNullOrEmpty()) {
                                    onNavigateToEdit(recognizedText!!)
                                } else {
                                    Toast.makeText(context, "Metin tanınamadı", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                val errorMessage = when {
                                    e.message?.contains("API key not valid") == true ->
                                        "API anahtarı geçersiz. Lütfen geçerli bir API anahtarı kullanın."
                                    e.message?.contains("network") == true ->
                                        "İnternet bağlantısı hatası. Lütfen bağlantınızı kontrol edin."
                                    else -> "Hata: ${e.localizedMessage}"
                                }
                                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                e.printStackTrace()
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = imageUri != null && !isLoading,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF8C7B)),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.size(80.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp))
                    } else {
                        Icon(Icons.Filled.Check, contentDescription = "Metni Tanı", tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Geri butonu (sol altta)
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomStart) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", modifier = Modifier.size(48.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewOcrScreen() {
    OcrScreen(
        onNavigateToEdit = { /* Yapılacak bir şey yok */ },
        onBackClick = { /* Yapılacak bir şey yok */ }
    )
}

fun uriToBitmap(contentResolver: ContentResolver, uri: Uri): Bitmap {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(contentResolver, uri)
        ImageDecoder.decodeBitmap(source)
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(contentResolver, uri)
    }
}

fun getLastImageUri(context: Context): Uri? {
    val projection = arrayOf(MediaStore.Images.Media._ID)
    val sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC"
    val cursor = context.contentResolver.query(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        sortOrder
    )
    cursor?.use {
        if (it.moveToFirst()) {
            val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id.toString())
        }
    }
    return null
} 