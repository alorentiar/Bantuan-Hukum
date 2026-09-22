# ProGuard / R8 rules for Google Play Release

# Google Play Services & AdMob
-keep public class com.google.android.gms.ads.** {
   public *;
}
-keep public class com.google.ads.** {
   public *;
}
-dontwarn com.google.android.gms.ads.**

# Room Database & SQLite FTS
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Moshi Models & Entity classes
-keep class com.example.data.model.** { *; }
-keepclassmembers class com.example.data.model.** { *; }
-keep class com.example.data.local.entity.** { *; }
-keepclassmembers class com.example.data.local.entity.** { *; }
