# MSAL
-keep class com.microsoft.identity.** { *; }
-keep class com.microsoft.aad.** { *; }
-dontwarn com.microsoft.identity.**
-dontwarn com.microsoft.aad.**
-keep class com.nimbusds.** { *; }
-dontwarn com.nimbusds.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Coroutines
-dontwarn kotlinx.coroutines.**

# Keep data classes used by Room
-keep class com.autologin.app.data.local.AuthEvent { *; }
