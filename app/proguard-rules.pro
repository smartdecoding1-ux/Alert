# Add project specific ProGuard rules here.
-keep class com.alert.app.data.models.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep class com.google.gson.** { *; }
