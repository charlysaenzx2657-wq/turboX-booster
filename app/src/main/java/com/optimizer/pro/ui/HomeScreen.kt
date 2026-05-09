package com.optimizer.pro.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.optimizer.pro.*
import com.optimizer.pro.ui.theme.TurboXColors
import kotlinx.coroutines.delay
import kotlin.math.sin

// ╔══════════════════════════════════════════════════════════════════════╗
// ║    TURBOX ULTRA — HOME SCREEN v3.0 CYBERPUNK NEON                   ║
// ╚══════════════════════════════════════════════════════════════════════╝

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToApps: () -> Unit,
    onNavigateToGames: () -> Unit,
    onNavigateToPro: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val shizukuStatus by viewModel.shizukuStatus.collectAsState()
    val isProMode = shizukuStatus == ShizukuStatus.CONNECTED

    // Animación de entrada global
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        visible = true
        while (true) { viewModel.refreshRamInfo(); delay(3000) }
    }
    LaunchedEffect(shizukuStatus) {
        if (shizukuStatus == ShizukuStatus.NO_PERMISSION || shizukuStatus == ShizukuStatus.NOT_RUNNING) {
            viewModel.connectShizuku()
        }
    }

    // Fondo animado con partículas
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val bgPhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing)),
        label = "bgPhase"
    )

    Box(modifier = Modifier.fillMaxSize().background(TurboXColors.BgDeep)) {
        // Fondo holográfico sutil
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawHolographicBackground(bgPhase, isProMode)
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        UltraTopBarTitle(isProMode = isProMode, bgPhase = bgPhase)
                    },
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(
                                Icons.Default.Settings, null,
                                tint = TurboXColors.TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = TurboXColors.BgDeep.copy(alpha = 0.95f),
                        scrolledContainerColor = TurboXColors.BgBase.copy(alpha = 0.98f)
                    )
                )
            }
        ) { padding ->
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 3 }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Spacer(Modifier.height(4.dp))

                    // Badge Pro Ultra
                    UltraModeBadge(isProMode = isProMode, onProClick = onNavigateToPro)

                    // RAM Card Ultra con gráfica
                    uiState.ramInfo?.let { UltraRamInfoCard(it) }

                    // Status Shizuku
                    ShizukuStatusCard(
                        status = shizukuStatus,
                        onActivate = { viewModel.connectShizuku() },
                        onRecheck = { viewModel.recheckShizuku() }
                    )

                    // Burbuja de optimización ULTRA
                    AnimatedVisibility(
                        visible = uiState.isOptimizing,
                        enter = scaleIn(spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium)) + fadeIn(tween(300)),
                        exit  = scaleOut(tween(250)) + fadeOut(tween(200))
                    ) {
                        UltraOptimizationBubble(
                            progress = uiState.optimizationProgress,
                            step = uiState.optimizationStep
                        )
                    }

                    // Grid módulos
                    AnimatedVisibility(
                        visible = !uiState.isOptimizing,
                        enter = fadeIn(tween(300)) + expandVertically(tween(350)),
                        exit  = fadeOut(tween(200)) + shrinkVertically(tween(250))
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                "⚡ OPTIMIZAR",
                                style = MaterialTheme.typography.labelLarge,
                                color = TurboXColors.TextTertiary,
                                letterSpacing = 2.sp
                            )
                            UltraOptimizationModulesGrid(
                                isProMode = isProMode,
                                isOptimizing = uiState.isOptimizing,
                                onOptimizeRam     = { viewModel.optimizeModule(OptModule.RAM) },
                                onOptimizeCache   = { viewModel.optimizeModule(OptModule.CACHE) },
                                onOptimizeCpu     = { viewModel.optimizeModule(OptModule.CPU) },
                                onOptimizeGpu     = { viewModel.optimizeModule(OptModule.GPU) },
                                onOptimizeFps     = { viewModel.optimizeModule(OptModule.FPS) },
                                onOptimizeNet     = { viewModel.optimizeModule(OptModule.NET) },
                                onOptimizeBattery = { viewModel.optimizeModule(OptModule.BATTERY) },
                                onOptimizeStorage = { viewModel.optimizeModule(OptModule.STORAGE) },
                                onOptimizeThermal = { viewModel.optimizeModule(OptModule.THERMAL) },
                                onOptimizeAll     = { viewModel.optimizeModule(OptModule.ALL) }
                            )

                            Text(
                                "ACCESO RÁPIDO",
                                style = MaterialTheme.typography.labelLarge,
                                color = TurboXColors.TextTertiary,
                                letterSpacing = 2.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                UltraQuickActionCard(
                                    icon = Icons.Default.SportsEsports,
                                    label = "MODO JUEGOS",
                                    accent = TurboXColors.NeonGreen,
                                    modifier = Modifier.weight(1f),
                                    onClick = onNavigateToGames
                                )
                                UltraQuickActionCard(
                                    icon = Icons.Default.Apps,
                                    label = "GESTIONAR APPS",
                                    accent = TurboXColors.NeonCyan,
                                    modifier = Modifier.weight(1f),
                                    onClick = onNavigateToApps
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    if (uiState.showResultDialog && uiState.lastResult != null) {
        UltraResultDialog(result = uiState.lastResult!!, onDismiss = { viewModel.dismissResultDialog() })
    }
}

// ═══════════════════════════════════════════════
// FONDO HOLOGRÁFICO
// ═══════════════════════════════════════════════
fun DrawScope.drawHolographicBackground(phase: Float, isPro: Boolean) {
    val color1 = if (isPro) Color(0x0A9B5DE5) else Color(0x060F3460)
    val color2 = if (isPro) Color(0x0600F5FF) else Color(0x05004080)
    val w = size.width
    val h = size.height
    val r = phase * (Math.PI / 180f).toFloat()

    // Orbes de luz sutiles
    drawCircle(
        brush = Brush.radialGradient(
            listOf(color1, Color.Transparent),
            center = Offset(w * 0.2f + w * 0.1f * sin(r.toDouble()).toFloat(), h * 0.15f),
            radius = w * 0.55f
        )
    )
    drawCircle(
        brush = Brush.radialGradient(
            listOf(color2, Color.Transparent),
            center = Offset(w * 0.85f + w * 0.05f * sin((r * 1.3f).toDouble()).toFloat(), h * 0.55f),
            radius = w * 0.45f
        )
    )
}

// ═══════════════════════════════════════════════
// TOP BAR TÍTULO ANIMADO
// ═══════════════════════════════════════════════
@Composable
fun UltraTopBarTitle(isProMode: Boolean, bgPhase: Float) {
    val shimmer by animateColorAsState(
        targetValue = if (isProMode) TurboXColors.NeonPurple else TurboXColors.NeonCyanDim,
        animationSpec = tween(800),
        label = "shimmer"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Icono con pulso
        val pulse = rememberInfiniteTransition(label = "iconPulse")
        val iconScale by pulse.animateFloat(
            initialValue = 0.9f, targetValue = 1.1f,
            animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
            label = "iconScale"
        )
        Icon(
            Icons.Default.Bolt, null,
            tint = shimmer,
            modifier = Modifier.size(24.dp).scale(if (isProMode) iconScale else 1f)
        )
        Text(
            "TurboX",
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            color = TurboXColors.TextPrimary,
            letterSpacing = (-0.5).sp
        )
        Text(
            if (isProMode) "ULTRA" else "BOOSTER",
            fontWeight = FontWeight.Black,
            fontSize = 20.sp,
            color = shimmer,
            letterSpacing = (-0.5).sp
        )
    }
}

// ═══════════════════════════════════════════════
// BADGE MODO
// ═══════════════════════════════════════════════
@Composable
fun UltraModeBadge(isProMode: Boolean, onProClick: () -> Unit) {
    val pulse = rememberInfiniteTransition(label = "badge")
    val glow by pulse.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )
    val color = if (isProMode) TurboXColors.NeonPurple else TurboXColors.NeonCyanDim
    val bgColor = if (isProMode)
        Brush.linearGradient(listOf(TurboXColors.NeonPurple.copy(0.18f), TurboXColors.NeonCyan.copy(0.08f)))
    else
        Brush.linearGradient(listOf(TurboXColors.BgCard, TurboXColors.BgSurfaceVariant))

    Surface(
        onClick = { if (!isProMode) onProClick() },
        shape = RoundedCornerShape(50.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, color.copy(if (isProMode) glow * 0.8f else 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(bgColor)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color.copy(if (isProMode) glow else 0.6f),
                                CircleShape
                            )
                    )
                    Text(
                        if (isProMode) "MODO ULTRA PRO ACTIVO" else "MODO NORMAL",
                        color = color,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
                if (isProMode) {
                    Text(
                        "SHIZUKU ✓",
                        color = TurboXColors.NeonGreen.copy(glow),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Activar Pro", color = TurboXColors.NeonCyanDim, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowForward, null, tint = TurboXColors.NeonCyanDim, modifier = Modifier.size(13.dp))
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
// RAM CARD ULTRA
// ═══════════════════════════════════════════════
@Composable
fun UltraRamInfoCard(ram: RamInfo) {
    val usagePct = (ram.usedMb.toFloat() / ram.totalMb.toFloat()).coerceIn(0f, 1f)
    val animPct by animateFloatAsState(targetValue = usagePct, animationSpec = tween(800), label = "ram")

    val barColor = when {
        usagePct > 0.85f -> TurboXColors.NeonRed
        usagePct > 0.65f -> TurboXColors.NeonOrange
        else             -> TurboXColors.NeonGreen
    }
    val animBarColor by animateColorAsState(targetValue = barColor, animationSpec = tween(600), label = "ramColor")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TurboXColors.BgCard),
        border = BorderStroke(1.dp, animBarColor.copy(0.2f))
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Memory, null, tint = animBarColor, modifier = Modifier.size(16.dp))
                    Text("MEMORIA RAM", color = TurboXColors.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${ram.usedMb}", color = animBarColor, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                    Text("/ ${ram.totalMb} MB", color = TurboXColors.TextTertiary, fontSize = 11.sp)
                }
            }

            // Barra ultra con gradiente
            Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(TurboXColors.BgSurface)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animPct)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(animBarColor.copy(0.7f), animBarColor)
                            )
                        )
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(6.dp).background(TurboXColors.NeonGreen, CircleShape))
                    Text("Libre: ${ram.availableMb} MB", color = TurboXColors.NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                Text(
                    "${(usagePct * 100).toInt()}% usado",
                    color = animBarColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                if (ram.isLowMemory) {
                    Text("⚠ RAM CRÍTICA", color = TurboXColors.NeonRed, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
// BURBUJA DE OPTIMIZACIÓN ULTRA
// ═══════════════════════════════════════════════
@Composable
fun UltraOptimizationBubble(progress: Float, step: String) {
    val animPct by animateFloatAsState(targetValue = progress, animationSpec = tween(600, easing = FastOutSlowInEasing), label = "pct")

    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue = 0.98f, targetValue = 1.02f,
        animationSpec = infiniteRepeatable(tween(800, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )
    val rotateArc by pulse.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing)),
        label = "arc"
    )

    val progressColor by animateColorAsState(
        targetValue = when {
            animPct >= 0.9f -> TurboXColors.NeonGreen
            animPct >= 0.5f -> TurboXColors.NeonCyan
            else            -> TurboXColors.NeonPurple
        },
        animationSpec = tween(500),
        label = "pColor"
    )

    val bgGradient = Brush.linearGradient(
        listOf(progressColor.copy(0.12f), progressColor.copy(0.04f))
    )

    Card(
        modifier = Modifier.fillMaxWidth().scale(scale),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, progressColor.copy(0.4f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(modifier = Modifier.background(bgGradient)) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    if (animPct >= 1f) "✅ OPTIMIZACIÓN COMPLETA" else "⚡ OPTIMIZANDO SISTEMA...",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = progressColor,
                    letterSpacing = 1.sp
                )

                Box(contentAlignment = Alignment.Center) {
                    // Track exterior
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.size(150.dp),
                        strokeWidth = 3.dp,
                        color = progressColor.copy(0.1f),
                        trackColor = progressColor.copy(0.08f)
                    )
                    // Track animado girando (decorativo)
                    CircularProgressIndicator(
                        progress = { 0.25f },
                        modifier = Modifier.size(150.dp).rotate(rotateArc),
                        strokeWidth = 2.dp,
                        color = progressColor.copy(0.25f),
                        trackColor = Color.Transparent
                    )
                    // Progreso real grueso
                    CircularProgressIndicator(
                        progress = { animPct },
                        modifier = Modifier.size(140.dp),
                        strokeWidth = 10.dp,
                        color = progressColor,
                        trackColor = progressColor.copy(0.1f),
                        strokeCap = StrokeCap.Round
                    )
                    // Número central
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "${(animPct * 100).toInt()}",
                            fontWeight = FontWeight.Black,
                            fontSize = 38.sp,
                            color = progressColor
                        )
                        Text("%", fontSize = 13.sp, color = progressColor.copy(0.6f), fontWeight = FontWeight.Bold)
                    }
                }

                if (step.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = progressColor.copy(0.1f)
                    ) {
                        Text(
                            step,
                            fontSize = 11.sp,
                            color = progressColor.copy(0.9f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
// GRID DE MÓDULOS ULTRA
// ═══════════════════════════════════════════════
data class UltraModuleItem(
    val emoji: String,
    val title: String,
    val subtitle: String,
    val enabled: Boolean,
    val accent: Color,
    val onClick: () -> Unit
)

@Composable
fun UltraOptimizationModulesGrid(
    isProMode: Boolean, isOptimizing: Boolean,
    onOptimizeRam: () -> Unit, onOptimizeCache: () -> Unit,
    onOptimizeCpu: () -> Unit, onOptimizeGpu: () -> Unit,
    onOptimizeFps: () -> Unit, onOptimizeNet: () -> Unit,
    onOptimizeBattery: () -> Unit, onOptimizeStorage: () -> Unit,
    onOptimizeThermal: () -> Unit,
    onOptimizeAll: () -> Unit
) {
    // Botón OPTIMIZAR TODO con efecto de shimmer
    val shimmer = rememberInfiniteTransition(label = "shimmerBtn")
    val shimmerOffset by shimmer.animateFloat(
        initialValue = -300f, targetValue = 600f,
        animationSpec = infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerOff"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(
                    listOf(TurboXColors.NeonPurple, TurboXColors.NeonCyan),
                    start = Offset(shimmerOffset, 0f),
                    end   = Offset(shimmerOffset + 400f, 200f)
                )
            )
            .clickable(enabled = !isOptimizing) { onOptimizeAll() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Bolt, null, tint = Color.Black, modifier = Modifier.size(20.dp))
            Text(
                if (isProMode) "⚡ OPTIMIZAR TODO ULTRA PRO" else "⚡ OPTIMIZAR TODO",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = Color.Black,
                letterSpacing = 0.5.sp
            )
        }
    }

    Spacer(Modifier.height(4.dp))

    val modules = listOf(
        UltraModuleItem("🧠","RAM",         "Mata procesos bg",      !isOptimizing,              TurboXColors.NeonPurple,  onOptimizeRam),
        UltraModuleItem("💾","Caché",       "Limpieza total",        !isOptimizing && isProMode, TurboXColors.NeonCyan,    onOptimizeCache),
        UltraModuleItem("⚡","CPU",          "Governor performance",  !isOptimizing && isProMode, TurboXColors.NeonYellow,  onOptimizeCpu),
        UltraModuleItem("🎮","GPU",          "Adreno/Mali max",       !isOptimizing && isProMode, TurboXColors.NeonGreen,   onOptimizeGpu),
        UltraModuleItem("🎯","FPS & Hz",    "120Hz + SkiaGL",        !isOptimizing && isProMode, TurboXColors.NeonOrange,  onOptimizeFps),
        UltraModuleItem("🌐","Red WiFi",    "TCP BBR + DNS",         !isOptimizing && isProMode, TurboXColors.NeonCyanDim, onOptimizeNet),
        UltraModuleItem("🔋","Batería",     "Doze agresivo",         !isOptimizing && isProMode, TurboXColors.NeonGreenDim,onOptimizeBattery),
        UltraModuleItem("💿","I/O Disco",   "Scheduler flash",       !isOptimizing && isProMode, TurboXColors.NeonPurpleBright, onOptimizeStorage),
        UltraModuleItem("🔥","Anti-Térmica","Sin throttling",        !isOptimizing && isProMode, TurboXColors.NeonRed,     onOptimizeThermal),
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        modules.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { mod ->
                    UltraModuleCard(mod = mod, isProMode = isProMode, modifier = Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun UltraModuleCard(mod: UltraModuleItem, isProMode: Boolean, modifier: Modifier = Modifier) {
    val isEnabled = mod.enabled
    val accent = if (isEnabled) mod.accent else TurboXColors.TextDisabled

    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "press"
    )

    Card(
        onClick = {
            if (isEnabled) { pressed = true; mod.onClick() }
        },
        enabled = isEnabled,
        modifier = modifier.height(86.dp).scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) accent.copy(0.08f) else TurboXColors.BgCard.copy(0.5f)
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (isEnabled) accent.copy(0.3f) else TurboXColors.TextDisabled.copy(0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(mod.emoji, fontSize = 20.sp)
                if (!isEnabled && mod.title != "RAM") {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = TurboXColors.NeonPurple.copy(0.15f)
                    ) {
                        Text(
                            "PRO",
                            color = TurboXColors.NeonPurple.copy(0.7f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Column {
                Text(mod.title, color = if (isEnabled) TurboXColors.TextPrimary else TurboXColors.TextDisabled, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(
                    if (!isProMode && mod.title != "RAM") "Requiere Pro" else mod.subtitle,
                    color = if (isEnabled) accent.copy(0.7f) else TurboXColors.TextDisabled.copy(0.4f),
                    fontSize = 9.sp, maxLines = 1
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════
// QUICK ACTION CARD ULTRA
// ═══════════════════════════════════════════════
@Composable
fun UltraQuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(76.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = accent.copy(0.08f)),
        border = BorderStroke(1.dp, accent.copy(0.25f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(36.dp).background(accent.copy(0.15f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(20.dp))
            }
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = TurboXColors.TextPrimary, letterSpacing = 0.3.sp)
        }
    }
}

// ═══════════════════════════════════════════════
// SHIZUKU STATUS CARD
// ═══════════════════════════════════════════════
@Composable
fun ShizukuStatusCard(status: ShizukuStatus, onActivate: () -> Unit, onRecheck: () -> Unit) {
    if (status == ShizukuStatus.CONNECTED) return
    val (text, btn) = when (status) {
        ShizukuStatus.NOT_RUNNING       -> "Shizuku no está activo" to true
        ShizukuStatus.NO_PERMISSION,
        ShizukuStatus.PERMISSION_DENIED -> "Sin permiso de Shizuku" to true
        ShizukuStatus.BINDING           -> "Conectando Modo Pro..." to false
        else                            -> "Modo Pro no activado" to true
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = TurboXColors.NeonOrange.copy(0.08f)),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, TurboXColors.NeonOrange.copy(0.3f))
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.WarningAmber, null, tint = TurboXColors.NeonOrange, modifier = Modifier.size(18.dp))
                Column {
                    Text("ESTADO MODO PRO", color = TurboXColors.TextTertiary, fontSize = 9.sp, letterSpacing = 1.sp)
                    Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TurboXColors.TextPrimary)
                }
            }
            if (btn) {
                Surface(
                    onClick = onActivate,
                    shape = RoundedCornerShape(8.dp),
                    color = TurboXColors.NeonOrange.copy(0.15f)
                ) {
                    Text("Activar", color = TurboXColors.NeonOrange, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                }
            } else {
                IconButton(onClick = onRecheck, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Refresh, null, tint = TurboXColors.NeonOrange, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
// RESULT DIALOG ULTRA
// ═══════════════════════════════════════════════
@Composable
fun UltraResultDialog(result: OptimizationResult, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = TurboXColors.BgCard,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, null, tint = TurboXColors.NeonGreen, modifier = Modifier.size(22.dp))
                Text("OPTIMIZACIÓN LISTA", fontWeight = FontWeight.ExtraBold, color = TurboXColors.NeonGreen, letterSpacing = 0.5.sp, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                Surface(shape = RoundedCornerShape(8.dp), color = TurboXColors.NeonPurple.copy(0.1f)) {
                    Text("Módulo: ${result.mode}", color = TurboXColors.NeonPurple, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp), letterSpacing = 0.5.sp)
                }
                if (result.ramFreedMb > 0) {
                    Surface(shape = RoundedCornerShape(8.dp), color = TurboXColors.NeonGreen.copy(0.1f)) {
                        Text("🧠 RAM liberada: ~${result.ramFreedMb} MB", fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp, color = TurboXColors.NeonGreen, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                    }
                }
                if (result.appsProcessed > 0) {
                    Text("Apps procesadas: ${result.appsProcessed}", fontSize = 12.sp, color = TurboXColors.TextSecondary)
                }
                HorizontalDivider(color = TurboXColors.TextTertiary.copy(0.2f), modifier = Modifier.padding(vertical = 4.dp))
                result.actions.take(20).forEach { action ->
                    Text(action, color = TurboXColors.TextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
                }
                if (result.actions.size > 20) {
                    Text("... y ${result.actions.size - 20} acciones más", color = TurboXColors.TextTertiary, fontSize = 10.sp)
                }
            }
        },
        confirmButton = {
            Surface(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                color = TurboXColors.NeonPurple.copy(0.15f)
            ) {
                Text("CERRAR", color = TurboXColors.NeonPurple, fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp, letterSpacing = 1.sp, modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
            }
        }
    )
}
