# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# MEMORY OPTIMIZATION PROGUARD RULES

# Basic Android rules
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# MEMORY OPTIMIZATIONS
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Remove logging in release builds to save memory
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Keep your app classes
-keep class id.istts.aplikasiadopsiterumbukarang.** { *; }

# RETROFIT & GSON OPTIMIZATIONS
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp3.** { *; }
-keep interface com.squareup.okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Exceptions

# GSON
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# GLIDE OPTIMIZATIONS
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder

# GOOGLE PLAY SERVICES OPTIMIZATIONS
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
-keep class com.google.android.libraries.places.** { *; }

# ANDROIDX OPTIMIZATIONS
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# NAVIGATION COMPONENT
-keepnames class androidx.navigation.fragment.NavHostFragment
-keep class * extends androidx.fragment.app.Fragment{}

# DATA BINDING & VIEW BINDING
-dontwarn android.databinding.**
-keep class android.databinding.** { *; }

# ROOM DATABASE
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# KOTLIN COROUTINES
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# REMOVE UNUSED RESOURCES (AGGRESSIVE MEMORY SAVING)
-keep class **.R
-keep class **.R$* {
    <fields>;
}

# KOTLIN METADATA (MEMORY OPTIMIZATION)
-dontwarn kotlin.Metadata
-dontwarn kotlin.jvm.internal.**
-dontwarn kotlin.reflect.**

# PARCELIZE
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# MATERIAL DESIGN
-dontwarn com.google.android.material.**
-keep class com.google.android.material.** { *; }

# ENUM OPTIMIZATION
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# SERIALIZATION
-keepattributes SerializedName
-keepattributes Signature
-keepattributes *Annotation*

# MEMORY LEAK PREVENTION
-dontwarn java.lang.invoke.**
-dontwarn **$$serializer
-keepclassmembers class **$WhenMappings {
    <fields>;
}

# REMOVE DEBUG INFORMATION IN RELEASE
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable