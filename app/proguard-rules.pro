# Add project specific ProGuard rules here.
-keep class com.turbox.optimizer.** { *; }
-keep class rikka.shizuku.** { *; }
-keep class moe.shizuku.** { *; }
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-dontwarn kotlin.**
-dontwarn kotlinx.**
