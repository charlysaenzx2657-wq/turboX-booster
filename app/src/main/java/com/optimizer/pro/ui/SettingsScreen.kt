package com.optimizer.pro.ui

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.optimizer.pro.*
import com.optimizer.pro.ui.theme.TurboXColors
import androidx.compose.animation.core.*
import androidx.compose.animation.*

// ╔══════════════════════════════════════════════════════════════════════╗
// ║    TURBOX ULTRA — SETTINGS SCREEN v3.0                               ║
// ╚══════════════════════════════════════════════════════════════════════╝

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val hasUsageStats = viewModel.hasUsageStatsPermission()
    val hasBatteryExemption = viewModel.hasBatteryOptimizationExemption()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        containerColor = TurboXColors.BgDeep,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Settings, null, tint = TurboXColors.NeonCyan, modifier = Modifier.size(20.dp))
                        Text("AJUSTES", fontWeight = FontWeight.Black, fontSize = 18.sp,
                            color = TurboXColors.TextPrimary, letterSpacing = 1.sp)
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
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(350)) + slideInVertically(tween(350)) { it / 4 }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SECTION: Permisos
                SettingsSectionHeader(icon = Icons.Default.Shield, title = "PERMISOS NECESARIOS", color = TurboXColors.NeonPurple)

                UltraPermissionItem(
                    icon = Icons.Default.QueryStats,
                    title = "Estadísticas de uso",
                    description = "Detecta qué apps consumen más batería y RAM para optimizarlas",
                    isGranted = hasUsageStats,
                    accentColor = TurboXColors.NeonCyan,
                    onRequest = { viewModel.openUsageStatsSettings(context) }
                )

                UltraPermissionItem(
                    icon = Icons.Default.BatteryChargingFull,
                    title = "Excluir de ahorro de batería",
                    description = "TurboX funciona en background sin restricciones de sistema",
                    isGranted = hasBatteryExemption,
                    accentColor = TurboXColors.NeonGreen,
                    onRequest = { viewModel.openBatteryOptimizationSettings(context) }
                )

                UltraPermissionItem(
                    icon = Icons.Default.Accessibility,
                    title = "Servicio de accesibilidad",
                    description = "Permite limpiar apps recientes automáticamente",
                    isGranted = false,
                    isOptional = true,
                    accentColor = TurboXColors.NeonOrange,
                    onRequest = { viewModel.openAccessibilitySettings(context) }
                )

                HorizontalDivider(color = TurboXColors.TextDisabled.copy(0.3f))

                // SECTION: Modo Pro
                SettingsSectionHeader(icon = Icons.Default.Bolt, title = "MODO PRO — SHIZUKU", color = TurboXColors.NeonPurple)

                UltraInfoCard(
                    icon = Icons.Default.Bolt,
                    title = "¿Qué es Shizuku?",
                    description = "Shizuku otorga permisos ADB (uid=2000) sin root. Permite controlar CPU, GPU, RAM, FPS y mucho más a fondo.\n\nInstala Shizuku desde Play Store → actívalo → abre TurboX.",
                    accentColor = TurboXColors.NeonPurple
                )

                // Pasos de configuración
                UltraSetupSteps()

                HorizontalDivider(color = TurboXColors.TextDisabled.copy(0.3f))

                // SECTION: Acerca de
                SettingsSectionHeader(icon = Icons.Default.Info, title = "ACERCA DE TURBOUX ULTRA", color = TurboXColors.NeonCyan)

                UltraInfoCard(
                    icon = Icons.Default.RocketLaunch,
                    title = "TurboX Ultra Booster v3.0",
                    description = "El optimizador Android más potente.\n• 17 módulos de optimización\n• CPU · GPU · RAM · Red · I/O · Térmica · FPS\n• Compatible con Qualcomm, MediaTek y Exynos\n• Sin root — vía Shizuku (ADB)",
                    accentColor = TurboXColors.NeonCyan
                )

                UltraInfoCard(
                    icon = Icons.Default.Security,
                    title = "Privacidad total",
                    description = "TurboX no recopila, envía ni vende ningún dato personal. Solo apps de Play Store son procesadas. Cero telemetría.",
                    accentColor = TurboXColors.NeonGreen
                )

                UltraInfoCard(
                    icon = Icons.Default.Shield,
                    title = "Seguridad garantizada",
                    description = "Shizuku usa permisos ADB estándar de Android. No modifica particiones de sistema. No requiere root. Todos los cambios son reversibles.",
                    accentColor = TurboXColors.NeonCyanDim
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(icon: ImageVector, title: String, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        Text(title, color = color, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.5.sp)
    }
}

@Composable
fun UltraPermissionItem(
    icon: ImageVector,
    title: String,
    description: String,
    isGranted: Boolean,
    isOptional: Boolean = false,
    accentColor: Color,
    onRequest: () -> Unit
) {
    val color = if (isGranted) TurboXColors.NeonGreen else accentColor

    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(if (isGranted) 0.07f else 0.04f)
        ),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, color.copy(if (isGranted) 0.35f else 0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).background(color.copy(0.12f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(title, color = TurboXColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    if (isOptional) {
                        Surface(shape = RoundedCornerShape(4.dp), color = TurboXColors.TextTertiary.copy(0.15f)) {
                            Text("Opcional", color = TurboXColors.TextTertiary, fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp))
                        }
                    }
                }
                Text(description, color = TurboXColors.TextTertiary, fontSize = 11.sp)
            }
            if (isGranted) {
                Icon(Icons.Default.CheckCircle, null, tint = TurboXColors.NeonGreen, modifier = Modifier.size(22.dp))
            } else {
                Surface(
                    onClick = onRequest,
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(0.15f)
                ) {
                    Text("Activar", color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                }
            }
        }
    }
}

@Composable
fun UltraSetupSteps() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = TurboXColors.NeonPurple.copy(0.05f)),
        border = BorderStroke(1.dp, TurboXColors.NeonPurple.copy(0.2f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.PlayArrow, null, tint = TurboXColors.NeonPurple, modifier = Modifier.size(16.dp))
                Text("PASOS PARA ACTIVAR PRO", color = TurboXColors.NeonPurple, fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            }
            listOf(
                "1️⃣" to "Instala Shizuku desde Play Store",
                "2️⃣" to "Activa Shizuku via ADB o modo desarrollador",
                "3️⃣" to "Abre TurboX → toca el badge \"Activar Pro\"",
                "4️⃣" to "Acepta el permiso de Shizuku cuando aparezca",
                "5️⃣" to "¡Listo! Ahora tienes acceso a todos los módulos Ultra"
            ).forEach { (step, text) ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(step, fontSize = 14.sp)
                    Text(text, color = TurboXColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun UltraInfoCard(icon: ImageVector, title: String, description: String, accentColor: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TurboXColors.BgCard),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, accentColor.copy(0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, null, tint = accentColor, modifier = Modifier.size(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(title, color = TurboXColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(description, color = TurboXColors.TextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
            }
        }
    }
}
