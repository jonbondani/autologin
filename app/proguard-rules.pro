# MSAL
-keep class com.microsoft.identity.** { *; }
-keep class com.microsoft.aad.** { *; }
-dontwarn com.microsoft.identity.**
-dontwarn com.microsoft.aad.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
