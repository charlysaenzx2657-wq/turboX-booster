package com.optimizer.pro.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.optimizer.pro.*
import com.optimizer.pro.ui.theme.TurboXColors

// ╔══════════════════════════════════════════════════════════════════════╗
// ║    TURBOX ULTRA — PRO SETUP SCREEN v3.0                              ║
// ╚══════════════════════════════════════════════════════════════════════╝

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProSetupScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val shizukuStatus by viewModel.shizukuStatus.collectAsState()
    val context = LocalContext.current
    val isConnected = shizukuStatus == ShizukuStatus.CONNECTED

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    // Fondo animado
    val infiniteT = rememberInfiniteTransition(label = "bg")
    val bgPhase by infiniteT.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(18000, easing = LinearEasing)),
        label = "phase"
    )

    Box(modifier = Modifier.fillMaxSize().background(TurboXColors.BgDeep)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawHolographicBackground(bgPhase, isConnected)
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text("MODO ULTRA PRO", fontWeight = FontWeight.Black, fontSize = 17.sp,
                            color = TurboXColors.TextPrimary, letterSpacing = 1.sp)
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, null, tint = TurboXColors.TextSecondary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
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
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    if (isConnected) {
                        UltraProActiveSuccess(onBack = onBack)
                    } else {
                        UltraProSetupHeader()
                        UltraModuleCountCard()
                        UltraModeComparisonCard()

                        Text("PASOS PARA ACTIVAR:", color = TurboXColors.TextTertiary,
                            fontWeight = FontWeight.ExtraBold, fontSize = 11.sp,
                            letterSpacing = 2.sp, modifier = Modifier.align(Alignment.Start))

                        UltraSetupStep(
                            number = 1, icon = Icons.Default.Download,
                            title = "Instala Shizuku",
                            description = "Descárgala gratis desde Play Store. App oficial, segura, sin root.",
                            actionLabel = "Ver en Play Store",
                            isCompleted = shizukuStatus != ShizukuStatus.NOT_INSTALLED,
                            accentColor = TurboXColors.NeonCyan,
                            onAction = {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW,
                                    android.net.Uri.parse("market://details?id=moe.shizuku.privileged.api"))
                                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                context.startActivity(intent)
                            }
                        )

                        UltraSetupStep(
                            number = 2, icon = Icons.Default.DeveloperMode,
                            title = "Activa depuración inalámbrica",
                            description = "Ajustes → Opciones de desarrollador → Depuración inalámbrica\n\n¿No ves opciones de desarrollador? Ajustes → Acerca del teléfono → toca 7× el número de compilación.",
                            actionLabel = "Ir a Ajustes",
                            isCompleted = false,
                            accentColor = TurboXColors.NeonYellow,
                            onAction = {
                                val intent = android.content.Intent(android.provider.Settings.ACTION_SETTINGS)
                                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                context.startActivity(intent)
                            }
                        )

                        UltraSetupStep(
                            number = 3, icon = Icons.Default.Bolt,
                            title = "Abre Shizuku y actívala",
                            description = "Abre la app Shizuku → toca 'Iniciar' usando depuración inalámbrica. Solo necesitas hacerlo UNA VEZ.",
                            actionLabel = "Abrir Shizuku",
                            isCompleted = shizukuStatus == ShizukuStatus.CONNECTED ||
                                          shizukuStatus == ShizukuStatus.NO_PERMISSION ||
                                          shizukuStatus == ShizukuStatus.READY,
                            accentColor = TurboXColors.NeonOrange,
                            onAction = {
                                try {
                                    val intent = context.packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
                                    intent?.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                    if (intent != null) context.startActivity(intent)
                                } catch (e: Exception) {}
                            }
                        )

                        UltraSetupStep(
                            number = 4, icon = Icons.Default.CheckCircle,
                            title = "Otorga permiso a TurboX Ultra",
                            description = "Con Shizuku activo, regresa aquí y toca el botón. Aparecerá un diálogo para aprobar — acepta.",
                            actionLabel = "Conectar ahora",
                            isCompleted = isConnected,
                            isHighlighted = true,
                            accentColor = TurboXColors.NeonPurple,
                            onAction = {
                                viewModel.connectShizuku()
                                viewModel.recheckShizuku()
                            }
                        )

                        // Botón principal CONECTAR
                        val shimmer = rememberInfiniteTransition(label = "btnShimmer")
                        val shimOff by shimmer.animateFloat(
                            initialValue = -400f, targetValue = 700f,
                            animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
                            label = "sOff"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(TurboXColors.NeonPurple, TurboXColors.NeonCyan),
                                        start = Offset(shimOff, 0f),
                                        end   = Offset(shimOff + 400f, 200f)
                                    )
                                )
                                .clickable {
                                    viewModel.recheckShizuku()
                                    viewModel.connectShizuku()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Bolt, null, tint = Color.Black, modifier = Modifier.size(22.dp))
                                Text("VERIFICAR Y CONECTAR SHIZUKU",
                                    color = Color.Black, fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp, letterSpacing = 0.5.sp)
                            }
                        }

                        UltraStatusIndicator(status = shizukuStatus)
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
// HEADER ULTRA PRO SETUP
// ═══════════════════════════════════════════════
@Composable
fun UltraProSetupHeader() {
    val pulse = rememberInfiniteTransition(label = "hPulse")
    val glow by pulse.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "g"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    Brush.radialGradient(listOf(TurboXColors.NeonPurple.copy(glow * 0.4f), Color.Transparent)),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        Brush.linearGradient(listOf(TurboXColors.NeonPurple, TurboXColors.NeonCyan)),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Bolt, null, tint = Color.Black, modifier = Modifier.size(36.dp))
            }
        }
        Text("DESBLOQUEA ULTRA PRO", color = TurboXColors.TextPrimary,
            fontWeight = FontWeight.Black, fontSize = 22.sp, letterSpacing = 0.5.sp)
        Text(
            "Optimizaciones de nivel profundo sin root.\nPermiso ADB vía Shizuku — 100% seguro.",
            color = TurboXColors.TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center
        )
    }
}

// ═══════════════════════════════════════════════
// MÓDULOS COUNT CARD
// ═══════════════════════════════════════════════
@Composable
fun UltraModuleCountCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TurboXColors.NeonPurple.copy(0.07f)),
        border = BorderStroke(1.dp, TurboXColors.NeonPurple.copy(0.25f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(
                "20" to "Módulos",
                "17" to "Optimiz.",
                "0" to "Root",
                "∞" to "FPS boost"
            ).forEach { (value, label) ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(value, color = TurboXColors.NeonPurple, fontSize = 22.sp, fontWeight = FontWeight.Black)
                    Text(label, color = TurboXColors.TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
// COMPARACIÓN MODOS
// ═══════════════════════════════════════════════
@Composable
fun UltraModeComparisonCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = TurboXColors.BgCard),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, TurboXColors.TextDisabled.copy(0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("COMPARACIÓN DE MODOS", color = TurboXColors.TextTertiary,
                fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // Normal
                Column(
                    modifier = Modifier.weight(1f).background(TurboXColors.NeonCyan.copy(0.06f), RoundedCornerShape(12.dp)).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("NORMAL", color = TurboXColors.NeonCyanDim, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                    listOf("Matar procesos bg", "Detectar apps pesadas", "Auto-optimizar boot", "RAM básico").forEach {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("•", color = TurboXColors.TextTertiary, fontSize = 10.sp)
                            Text(it, color = TurboXColors.TextSecondary, fontSize = 10.sp)
                        }
                    }
                }
                // Pro
                Column(
                    modifier = Modifier.weight(1f)
                        .background(
                            Brush.linearGradient(listOf(TurboXColors.NeonPurple.copy(0.12f), TurboXColors.NeonCyan.copy(0.06f))),
                            RoundedCornerShape(12.dp)
                        )
                        .border(1.dp, TurboXColors.NeonPurple.copy(0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("⚡ ULTRA PRO", color = TurboXColors.NeonPurple, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                    listOf("CPU Governor MAX", "GPU Adreno/Mali MAX", "RAM + ZRAM + KSM", "FPS 144Hz + SkiaVK",
                        "TCP BBR2 gaming", "I/O none scheduler", "ART JIT 64MB", "Kernel tunables",
                        "+ 12 módulos más").forEach {
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("⚡", fontSize = 9.sp)
                            Text(it, color = TurboXColors.TextPrimary, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
// SETUP STEP ULTRA
// ═══════════════════════════════════════════════
@Composable
fun UltraSetupStep(
    number: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    actionLabel: String,
    isCompleted: Boolean,
    isHighlighted: Boolean = false,
    accentColor: Color,
    onAction: () -> Unit
) {
    val borderColor = when {
        isCompleted   -> TurboXColors.NeonGreen
        isHighlighted -> accentColor
        else          -> TurboXColors.TextDisabled.copy(0.2f)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) TurboXColors.NeonGreen.copy(0.05f)
                             else if (isHighlighted) accentColor.copy(0.06f)
                             else TurboXColors.BgCard
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, borderColor.copy(if (isCompleted || isHighlighted) 0.5f else 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Número o check
            Box(
                modifier = Modifier.size(36.dp)
                    .background(
                        if (isCompleted) TurboXColors.NeonGreen.copy(0.15f) else accentColor.copy(0.12f),
                        CircleShape
                    )
                    .border(1.dp, if (isCompleted) TurboXColors.NeonGreen.copy(0.5f) else accentColor.copy(0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(Icons.Default.Check, null, tint = TurboXColors.NeonGreen, modifier = Modifier.size(18.dp))
                } else {
                    Text("$number", color = accentColor, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                }
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = if (isCompleted) TurboXColors.NeonGreen else accentColor, modifier = Modifier.size(14.dp))
                    Text(title, color = TurboXColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Text(description, color = TurboXColors.TextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
                if (!isCompleted) {
                    Surface(
                        onClick = onAction,
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor.copy(0.12f),
                        border = BorderStroke(1.dp, accentColor.copy(0.4f))
                    ) {
                        Text(actionLabel, color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
// STATUS INDICATOR ULTRA
// ═══════════════════════════════════════════════
@Composable
fun UltraStatusIndicator(status: ShizukuStatus) {
    val (text, color) = when (status) {
        ShizukuStatus.NOT_INSTALLED     -> "Shizuku no instalado" to TurboXColors.NeonOrange
        ShizukuStatus.NOT_RUNNING       -> "Shizuku no está activo aún" to TurboXColors.NeonYellow
        ShizukuStatus.NO_PERMISSION     -> "Esperando que apruebes el permiso..." to TurboXColors.NeonCyanDim
        ShizukuStatus.BINDING           -> "Conectando..." to TurboXColors.NeonCyan
        ShizukuStatus.CONNECTED         -> "¡Conectado! Modo Ultra Pro activo ✅" to TurboXColors.NeonGreen
        else                            -> "Estado: $status" to TurboXColors.TextTertiary
    }
    val pulse = rememberInfiniteTransition(label = "status")
    val alpha by pulse.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "a"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = color.copy(0.08f),
        border = BorderStroke(1.dp, color.copy(0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(8.dp).background(color.copy(alpha), CircleShape))
            Spacer(Modifier.width(10.dp))
            Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ═══════════════════════════════════════════════
// ÉXITO PRO ACTIVADO
// ═══════════════════════════════════════════════
@Composable
fun UltraProActiveSuccess(onBack: () -> Unit) {
    val pulse = rememberInfiniteTransition(label = "success")
    val glow by pulse.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "g"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.padding(top = 40.dp)
    ) {
        // Icono animado con glow
        Box(
            modifier = Modifier.size(100.dp)
                .background(TurboXColors.NeonGreen.copy(glow * 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(76.dp)
                    .background(TurboXColors.NeonGreen.copy(0.15f), CircleShape)
                    .border(2.dp, TurboXColors.NeonGreen.copy(glow * 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CheckCircle, null, tint = TurboXColors.NeonGreen, modifier = Modifier.size(54.dp))
            }
        }

        Text("¡ULTRA PRO ACTIVO! ⚡", color = TurboXColors.NeonGreen,
            fontWeight = FontWeight.Black, fontSize = 24.sp, letterSpacing = 0.5.sp)

        Text(
            "Shizuku conectado.\nTodos los 20 módulos de optimización\nestán desbloqueados.",
            color = TurboXColors.TextSecondary, fontSize = 14.sp, textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        // Stats de módulos
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                "⚡" to "CPU MAX",
                "🎮" to "GPU MAX",
                "🧠" to "RAM+ZRAM",
                "🎯" to "FPS 144Hz"
            ).forEach { (emoji, label) ->
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = TurboXColors.NeonGreen.copy(0.08f),
                    border = BorderStroke(1.dp, TurboXColors.NeonGreen.copy(0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(emoji, fontSize = 16.sp)
                        Text(label, color = TurboXColors.NeonGreen, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.3.sp)
                    }
                }
            }
        }

        // Botón volver a optimizar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(TurboXColors.NeonGreen, TurboXColors.NeonCyan)))
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Bolt, null, tint = Color.Black, modifier = Modifier.size(20.dp))
                Text("EMPEZAR A OPTIMIZAR ULTRA", color = Color.Black,
                    fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, letterSpacing = 0.5.sp)
            }
        }
    }
}
