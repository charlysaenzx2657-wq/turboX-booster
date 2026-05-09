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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.optimizer.pro.*
import com.optimizer.pro.ui.theme.TurboXColors

// ╔══════════════════════════════════════════════════════════════════════╗
// ║    TURBOX ULTRA — GAMES SCREEN v3.0                                  ║
// ╚══════════════════════════════════════════════════════════════════════╝

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val uiState by viewModel.uiState.collectAsState()
    val shizukuStatus by viewModel.shizukuStatus.collectAsState()
    val isProMode = shizukuStatus == ShizukuStatus.CONNECTED

    val allApps = uiState.installedApps
    val games = allApps.filter { app ->
        val name = app.appName.lowercase()
        val pkg  = app.packageName.lowercase()
        val keywords = listOf("clash","pubg","freefire","roblox","minecraft","fortnite",
            "arena","mobile","legend","cod","genshin","mlbb","brawl","legends","racing",
            "battle","shooter","zombie","survival","rpg","moba","fps","war","strike",
            "dragon","ninja","sword","chess","poker","casino","ludo","quiz","trivia",
            "gun","sniper","tank","bike","car","drift","soccer","football","basketball",
            "game","games","play","pixel","craft","hero","king","quest","adventure")
        name.contains("game") || name.contains("juego") ||
        pkg.contains("game") || pkg.contains("games") ||
        keywords.any { name.contains(it) || pkg.contains(it) }
    }
    val otherApps = allApps.filter { it !in games }

    Scaffold(
        containerColor = TurboXColors.BgDeep,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val pulse = rememberInfiniteTransition(label = "gameIcon")
                        val iconScale by pulse.animateFloat(
                            initialValue = 0.9f, targetValue = 1.1f,
                            animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
                            label = "s"
                        )
                        Icon(Icons.Default.SportsEsports, null, tint = TurboXColors.NeonGreen,
                            modifier = Modifier.size(22.dp).scale(iconScale))
                        Text("MODO JUEGOS", fontWeight = FontWeight.Black, fontSize = 19.sp,
                            color = TurboXColors.TextPrimary, letterSpacing = 0.5.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = TurboXColors.TextSecondary)
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
            // MODO JUEGO GLOBAL
            item {
                UltraGameModeStatusCard(
                    isActive = uiState.isGameModeActive,
                    isProMode = isProMode,
                    onActivate = { viewModel.activateGameMode() },
                    onDeactivate = { viewModel.deactivateGameMode() }
                )
            }

            // STATS JUEGOS
            if (games.isNotEmpty()) {
                item {
                    UltraGamesHeader(count = games.size, isProMode = isProMode)
                }
                items(games, key = { it.packageName }) { app ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(300)) + slideInHorizontally(tween(300)) { -it / 4 }
                    ) {
                        UltraGameAppItem(
                            app = app,
                            isActive = uiState.activeGamePackage == app.packageName,
                            isOptimizing = uiState.isOptimizing,
                            isProMode = isProMode,
                            onBoost = {
                                if (uiState.activeGamePackage == app.packageName)
                                    viewModel.deactivateGameMode()
                                else
                                    viewModel.boostGame(app.packageName)
                            }
                        )
                    }
                }
            } else {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🎮", fontSize = 48.sp)
                            Text("No se detectaron juegos", color = TurboXColors.TextSecondary, fontSize = 14.sp)
                            Text("Instala juegos para optimizarlos aquí", color = TurboXColors.TextTertiary, fontSize = 12.sp)
                        }
                    }
                }
            }

            // OTRAS APPS
            if (otherApps.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "📱 OTRAS APPS (${otherApps.size})",
                            color = TurboXColors.TextTertiary, fontSize = 11.sp,
                            fontWeight = FontWeight.Bold, letterSpacing = 1.sp
                        )
                        Text("Boost de FPS disponible", color = TurboXColors.NeonCyanDim.copy(0.6f), fontSize = 9.sp)
                    }
                }
                items(otherApps.take(30), key = { it.packageName }) { app ->
                    UltraGameAppItem(
                        app = app,
                        isActive = uiState.activeGamePackage == app.packageName,
                        isOptimizing = uiState.isOptimizing,
                        isProMode = isProMode,
                        onBoost = {
                            if (uiState.activeGamePackage == app.packageName)
                                viewModel.deactivateGameMode()
                            else
                                viewModel.boostAppFps(app.packageName)
                        }
                    )
                }
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
fun UltraGamesHeader(count: Int, isProMode: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = TurboXColors.NeonGreen.copy(0.06f)),
        border = BorderStroke(1.dp, TurboXColors.NeonGreen.copy(0.2f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("🎮 $count JUEGOS DETECTADOS", color = TurboXColors.NeonGreen,
                    fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                Text("Boost individual disponible por juego", color = TurboXColors.TextTertiary, fontSize = 10.sp)
            }
            if (isProMode) {
                Surface(shape = RoundedCornerShape(8.dp), color = TurboXColors.NeonGreen.copy(0.15f)) {
                    Text("PRO", color = TurboXColors.NeonGreen, fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
fun UltraGameModeStatusCard(
    isActive: Boolean, isProMode: Boolean,
    onActivate: () -> Unit, onDeactivate: () -> Unit
) {
    val pulse = rememberInfiniteTransition(label = "gamePulse")
    val glow by pulse.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "g"
    )
    val activeColor = TurboXColors.NeonGreen
    val inactiveColor = TurboXColors.NeonCyanDim
    val color = if (isActive) activeColor else inactiveColor

    val bgGrad = if (isActive)
        Brush.linearGradient(listOf(TurboXColors.NeonGreen.copy(0.15f), TurboXColors.NeonCyan.copy(0.06f)))
    else
        Brush.linearGradient(listOf(TurboXColors.BgCard, TurboXColors.BgSurfaceVariant.copy(0.5f)))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, color.copy(if (isActive) glow * 0.7f else 0.25f))
    ) {
        Box(modifier = Modifier.background(bgGrad)) {
            Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(color.copy(if (isActive) glow else 0.5f), CircleShape)
                        )
                        Column {
                            Text(if (isActive) "🎮 MODO JUEGO ACTIVO" else "MODO JUEGO", color = color,
                                fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                            Text(if (isActive) "CPU/GPU/Hz/RAM al máximo" else "Activa para máximo rendimiento",
                                color = TurboXColors.TextTertiary, fontSize = 10.sp)
                        }
                    }
                }

                if (isActive) {
                    // Stats activos
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "⚡ CPU" to "MAX",
                            "🎮 GPU" to "MAX",
                            "🎯 FPS" to "120Hz",
                            "🧠 RAM" to "LIBRE"
                        ).forEach { (label, value) ->
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                color = TurboXColors.NeonGreen.copy(0.1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(label, color = TurboXColors.TextTertiary, fontSize = 8.sp, letterSpacing = 0.2.sp)
                                    Text(value, color = TurboXColors.NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isActive) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Brush.linearGradient(listOf(TurboXColors.NeonGreen, TurboXColors.NeonCyan)))
                                .clickable(onClick = onActivate),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                Text("ACTIVAR MODO JUEGO", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = onDeactivate,
                            modifier = Modifier.fillMaxWidth().height(42.dp),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, TurboXColors.NeonRed.copy(0.5f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TurboXColors.NeonRed)
                        ) {
                            Icon(Icons.Default.Stop, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("DESACTIVAR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UltraGameAppItem(
    app: AppInfo,
    isActive: Boolean,
    isOptimizing: Boolean,
    isProMode: Boolean,
    onBoost: () -> Unit
) {
    val accent = if (isActive) TurboXColors.NeonGreen else TurboXColors.NeonPurple
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.01f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "itemScale"
    )

    Card(
        modifier = Modifier.fillMaxWidth().scale(scale),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (isActive) TurboXColors.NeonGreen.copy(0.08f) else TurboXColors.BgCard),
        border = BorderStroke(1.dp, if (isActive) TurboXColors.NeonGreen.copy(0.4f) else TurboXColors.TextDisabled.copy(0.15f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de app
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
                    .background(TurboXColors.BgSurface),
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
                        modifier = Modifier.size(38.dp).clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Text("🎮", fontSize = 22.sp)
                }
            }

            Column(Modifier.weight(1f)) {
                Text(
                    app.appName,
                    color = TurboXColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    app.packageName,
                    color = TurboXColors.TextTertiary,
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isActive) {
                    Text("⚡ BOOST ACTIVO", color = TurboXColors.NeonGreen, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                }
            }

            // Botón boost
            Surface(
                onClick = { if (!isOptimizing) onBoost() },
                shape = RoundedCornerShape(8.dp),
                color = if (isActive) TurboXColors.NeonRed.copy(0.15f) else accent.copy(0.15f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (isActive) Icons.Default.Stop else Icons.Default.Bolt,
                        null,
                        tint = if (isActive) TurboXColors.NeonRed else accent,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        if (isActive) "STOP" else "BOOST",
                        color = if (isActive) TurboXColors.NeonRed else accent,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
