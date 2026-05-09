package com.optimizer.pro.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.optimizer.pro.*
import com.optimizer.pro.ui.theme.TurboXColors

// ╔══════════════════════════════════════════════════════════════════════╗
// ║    TURBOX ULTRA — APPS SCREEN v3.0                                   ║
// ╚══════════════════════════════════════════════════════════════════════╝

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val shizukuStatus by viewModel.shizukuStatus.collectAsState()
    val isProMode = shizukuStatus == ShizukuStatus.CONNECTED
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(AppFilter.ALL) }

    val filteredApps = uiState.installedApps.filter {
        searchQuery.isBlank() || it.appName.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        containerColor = TurboXColors.BgDeep,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Apps, null, tint = TurboXColors.NeonCyan, modifier = Modifier.size(20.dp))
                        Text("GESTIONAR APPS", fontWeight = FontWeight.Black, fontSize = 18.sp,
                            color = TurboXColors.TextPrimary, letterSpacing = 1.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = TurboXColors.TextSecondary)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadInstalledApps() }) {
                        Icon(Icons.Default.Refresh, null, tint = TurboXColors.NeonCyanDim)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = TurboXColors.BgDeep)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // INFO HEADER
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = TurboXColors.NeonCyan.copy(0.06f)),
                    border = BorderStroke(1.dp, TurboXColors.NeonCyan.copy(0.2f))
                ) {
                    Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(44.dp).background(TurboXColors.NeonCyan.copy(0.12f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Apps, null, tint = TurboXColors.NeonCyan, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text("${uiState.installedApps.size} APPS INSTALADAS",
                                fontWeight = FontWeight.ExtraBold, fontSize = 13.sp,
                                color = TurboXColors.TextPrimary, letterSpacing = 0.5.sp)
                            Text(
                                if (isProMode) "Pro: caché + force-stop + boost FPS individual"
                                else "Normal: matar procesos en background",
                                color = TurboXColors.TextTertiary, fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // BUSCADOR
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar app...", color = TurboXColors.TextTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = TurboXColors.NeonCyanDim) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, null, tint = TurboXColors.TextTertiary, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TurboXColors.NeonCyan.copy(0.5f),
                        unfocusedBorderColor = TurboXColors.TextDisabled.copy(0.3f),
                        focusedTextColor = TurboXColors.TextPrimary,
                        unfocusedTextColor = TurboXColors.TextPrimary,
                        cursorColor = TurboXColors.NeonCyan,
                        focusedContainerColor = TurboXColors.BgCard,
                        unfocusedContainerColor = TurboXColors.BgCard
                    )
                )
            }

            // ACCIONES MASIVAS
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ACCIONES MASIVAS", style = MaterialTheme.typography.labelLarge,
                        color = TurboXColors.TextTertiary, letterSpacing = 2.sp)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Matar bg
                        Surface(
                            onClick = { viewModel.optimizeModule(OptModule.RAM) },
                            shape = RoundedCornerShape(10.dp),
                            color = TurboXColors.NeonPurple.copy(0.12f),
                            border = BorderStroke(1.dp, TurboXColors.NeonPurple.copy(0.3f)),
                            modifier = Modifier.weight(1f).height(42.dp)
                        ) {
                            Row(
                                Modifier.fillMaxSize().padding(horizontal = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Memory, null, tint = TurboXColors.NeonPurple, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(5.dp))
                                Text("Matar BG", color = TurboXColors.NeonPurple, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (isProMode) {
                            Surface(
                                onClick = { viewModel.optimizeModule(OptModule.CACHE) },
                                shape = RoundedCornerShape(10.dp),
                                color = TurboXColors.NeonCyan.copy(0.12f),
                                border = BorderStroke(1.dp, TurboXColors.NeonCyan.copy(0.3f)),
                                modifier = Modifier.weight(1f).height(42.dp)
                            ) {
                                Row(
                                    Modifier.fillMaxSize().padding(horizontal = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CleaningServices, null, tint = TurboXColors.NeonCyan, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Text("Caché", color = TurboXColors.NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    if (isProMode) {
                        // Boost FPS global con gradiente
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(TurboXColors.NeonGreen.copy(0.8f), TurboXColors.NeonCyan.copy(0.8f))))
                                .clickable { viewModel.optimizeModule(OptModule.FPS) },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Speed, null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Text("⚡ BOOST FPS GLOBAL (GPU + 120Hz + Animaciones)", color = Color.Black,
                                    fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.3.sp)
                            }
                        }
                    }
                }
            }

            // CONTADOR
            item {
                Text(
                    "${filteredApps.size} APPS ${if (searchQuery.isNotEmpty()) "ENCONTRADAS" else "TOTALES"}",
                    color = TurboXColors.TextTertiary, fontSize = 10.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                )
            }

            // LISTA DE APPS
            items(filteredApps, key = { it.packageName }) { app ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(200)) + slideInHorizontally(tween(200)) { -it / 6 }
                ) {
                    UltraAppListItem(
                        app = app,
                        isProMode = isProMode,
                        onKill = { viewModel.optimizeApp(app.packageName) },
                        onBoostFps = { viewModel.boostAppFps(app.packageName) }
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }

    if (uiState.showResultDialog && uiState.lastResult != null) {
        UltraResultDialog(result = uiState.lastResult!!, onDismiss = { viewModel.dismissResultDialog() })
    }
}

enum class AppFilter { ALL, GAMES, HEAVY }

@Composable
fun UltraAppListItem(app: AppInfo, isProMode: Boolean, onKill: () -> Unit, onBoostFps: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = TurboXColors.BgCard),
        border = BorderStroke(1.dp, TurboXColors.TextDisabled.copy(0.12f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono + nombre
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(TurboXColors.BgSurface),
                    contentAlignment = Alignment.Center
                ) {
                    if (app.icon != null) {
                        val bmp = remember(app.packageName) {
                            android.graphics.Bitmap.createBitmap(
                                app.icon.intrinsicWidth.coerceAtLeast(1),
                                app.icon.intrinsicHeight.coerceAtLeast(1),
                                android.graphics.Bitmap.Config.ARGB_8888
                            ).also { bmp ->
                                val canvas = android.graphics.Canvas(bmp)
                                app.icon.setBounds(0, 0, canvas.width, canvas.height)
                                app.icon.draw(canvas)
                            }
                        }
                        androidx.compose.foundation.Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(34.dp).clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        Icon(Icons.Default.Android, null, tint = TurboXColors.NeonPurple.copy(0.6f), modifier = Modifier.size(22.dp))
                    }
                }
                Column {
                    Text(app.appName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                        color = TurboXColors.TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(app.packageName, color = TurboXColors.TextTertiary, fontSize = 9.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            // Botones de acción
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (isProMode) {
                    Surface(
                        onClick = onBoostFps,
                        shape = RoundedCornerShape(8.dp),
                        color = TurboXColors.NeonGreen.copy(0.12f),
                        border = BorderStroke(1.dp, TurboXColors.NeonGreen.copy(0.3f))
                    ) {
                        Icon(Icons.Default.Speed, null, tint = TurboXColors.NeonGreen,
                            modifier = Modifier.padding(6.dp).size(15.dp))
                    }
                }
                Surface(
                    onClick = onKill,
                    shape = RoundedCornerShape(8.dp),
                    color = TurboXColors.NeonRed.copy(0.1f),
                    border = BorderStroke(1.dp, TurboXColors.NeonRed.copy(0.25f))
                ) {
                    Icon(Icons.Default.Close, null, tint = TurboXColors.NeonRed,
                        modifier = Modifier.padding(6.dp).size(15.dp))
                }
            }
        }
    }
}
