# TurboX Booster ProGuard Rules

# Keep Shizuku
-keep class rikka.shizuku.** { *; }
-keep class moe.shizuku.** { *; }

# Keep AIDL
-keep class com.optimizer.pro.IShizukuService { *; }
-keep class com.optimizer.pro.IShizukuService$Stub { *; }
-keep class com.optimizer.pro.ShizukuServiceImpl { *; }

# Keep data classes
-keep class com.optimizer.pro.OptimizationResult { *; }
-keep class com.optimizer.pro.RamInfo { *; }
-keep class com.optimizer.pro.AppInfo { *; }

# Kotlin
-keep class kotlin.** { *; }
-keepclassmembers class kotlin.Metadata { *; }
