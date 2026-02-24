# MSAL - keep only public API and required internals
-keep public class com.microsoft.identity.client.** {
    public *;
}
-keep class com.microsoft.identity.common.** { *; }
-keep class com.microsoft.aad.** { *; }
-dontwarn com.microsoft.identity.**
-dontwarn com.microsoft.aad.**
-keep class com.nimbusds.** { *; }
-dontwarn com.nimbusds.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep class com.autologin.app.data.local.AuthEvent { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Coroutines
-dontwarn kotlinx.coroutines.**

# SQLCipher
-keep class net.sqlcipher.** { *; }
-dontwarn net.sqlcipher.**
