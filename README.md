# ⚡ TurboX Booster

**Optimizador de sistema avanzado para Android** — sin root, con soporte para Shizuku y ADB.

[![Build APK](https://github.com/TU_USUARIO/TurboXBooster/actions/workflows/build.yml/badge.svg)](https://github.com/TU_USUARIO/TurboXBooster/actions/workflows/build.yml)
![Android](https://img.shields.io/badge/Android-8.0%2B-green)
![Kotlin](https://img.shields.io/badge/Kotlin-1.9-blue)
![No Root](https://img.shields.io/badge/Root-No%20Requerido-brightgreen)

---

## 📱 Funcionalidades

### Dashboard (sin root)
- CPU: uso en tiempo real, frecuencias por núcleo, governor, gráfica histórica
- RAM: uso, memoria disponible, SWAP, gráfica histórica
- Temperatura del procesador con alertas
- Batería: nivel, voltaje, estado, temperatura
- Almacenamiento interno
- Score del sistema (0–100)

### Optimizador de 1 toque
- Garbage Collection agresivo (sin root)
- Cerrar apps en background (Shizuku)
- Desactivar animaciones del sistema (Shizuku)
- Activar aceleración GPU (Shizuku)
- Limpiar caché del sistema (Shizuku)
- Liberar throttling térmico (Shizuku)

### Game Booster
- Detección automática de juegos instalados
- 4 perfiles: Ahorro / Balanceado / Performance / Extremo
- Lanzar juego con boost previo (kill background + GPU)
- Game Mode de Android 12+ vía Shizuku

### Avanzado (Shizuku / ADB)
- Control de escalas de animación (0x–2x) con sliders
- Forzar renderizado GPU / HW UI
- Límite de procesos en background
- Modo performance del sistema
- Cambiar DNS (Cloudflare, Google, AdGuard)
- Optimización TCP/IP
- Terminal Shizuku integrado
- Cambiar densidad de pantalla (DPI)
- Activar opciones de desarrollador

### Kernel Tweaks (Shizuku)
- Info en vivo: kernel version, uptime, load average, I/O scheduler, TCP congestion
- Tweaks VM para gaming (swappiness bajo, dirty_ratio)
- Tweaks TCP/IP (buffers grandes, BBR congestion control)
- Cambiar I/O scheduler (deadline / cfq / noop)
- Aumentar read-ahead (2MB)

---

## 🚀 Compilar el APK

### Opción 1: GitHub Actions (recomendado)
1. Sube el proyecto a un repositorio de GitHub
2. Ve a **Actions** → el workflow `Build TurboX Booster APK` corre automáticamente
3. Descarga el APK en **Actions → tu último build → Artifacts**

### Opción 2: Local con Android Studio
```bash
git clone https://github.com/TU_USUARIO/TurboXBooster
cd TurboXBooster
./gradlew assembleDebug
# APK en: app/build/outputs/apk/debug/app-debug.apk
```

### Opción 3: Línea de comandos
```bash
# Debug
./gradlew assembleDebug

# Release (sin firmar)
./gradlew assembleRelease
```

**Requisitos:** JDK 17, Android SDK (API 34), Gradle 8.4

---

## 🔒 Cómo activar Shizuku

Shizuku permite hacer todas las optimizaciones avanzadas **sin root**. Se activa una sola vez:

### Método 1: ADB por USB (PC)
```bash
adb shell sh /sdcard/Android/data/moe.shizuku.privileged.api/start.sh
```

### Método 2: Depuración inalámbrica (Android 11+, sin PC)
1. Ajustes → Opciones de desarrollador → Depuración inalámbrica → Activar
2. Toca "Emparejar dispositivo con código de emparejamiento"
3. En Shizuku → "Inicio por depuración inalámbrica"
4. Ingresa el código que aparece

### Método 3: ADB inalámbrico automático (Android 11+)
Una vez configurado ADB inalámbrico, Shizuku se puede autoiniciar en cada arranque.

---

## 📋 Permisos que usa la app

| Permiso | Para qué |
|---|---|
| KILL_BACKGROUND_PROCESSES | Cerrar apps en background |
| WRITE_SECURE_SETTINGS | Cambiar animaciones, DNS, ajustes |
| DUMP | Leer info detallada del sistema |
| CHANGE_CONFIGURATION | Cambiar densidad de pantalla |
| QUERY_ALL_PACKAGES | Detectar juegos instalados |
| FOREGROUND_SERVICE | Monitoreo en segundo plano |

> Los permisos marcados con 🔒 los concede Shizuku automáticamente, no hay que hacer nada manual.

---

## 🏗️ Estructura del proyecto

```
TurboXBooster/
├── .github/workflows/build.yml     # CI/CD GitHub Actions
├── app/
│   ├── src/main/
│   │   ├── aidl/                   # IUserService.aidl (interfaz Shizuku)
│   │   ├── java/com/turbox/optimizer/
│   │   │   ├── MainActivity.kt
│   │   │   ├── TurboXApp.kt
│   │   │   ├── service/            # OptimizerService, ShizukuUserService
│   │   │   ├── receiver/           # BootReceiver
│   │   │   ├── ui/                 # Fragments (Dashboard, Optimizer, Gaming, Advanced, Kernel)
│   │   │   └── utils/              # SystemUtils, ShizukuHelper, GameUtils, KernelUtils...
│   │   └── res/                    # Layouts, colores, temas, iconos, strings
│   └── build.gradle
└── build.gradle
```

---

## 📄 Licencia

MIT License — úsalo, modifícalo y distribúyelo libremente.

---

> **TurboX Booster** — Hecho con ❤️ para Android
