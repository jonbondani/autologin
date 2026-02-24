package com.autologin.app.di

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.room.Room
import com.autologin.app.data.local.AuthEventDao
import com.autologin.app.data.local.AuthEventDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SupportFactory
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val KEYSTORE_ALIAS = "autologin_db_key"

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AuthEventDatabase {
        val passphrase = getOrCreateDbKey(context)
        val factory = SupportFactory(passphrase)
        return Room.databaseBuilder(
            context,
            AuthEventDatabase::class.java,
            "autologin.db",
        ).openHelperFactory(factory).build()
    }

    @Provides
    fun provideAuthEventDao(database: AuthEventDatabase): AuthEventDao {
        return database.authEventDao()
    }

    private fun getOrCreateDbKey(context: Context): ByteArray {
        // Try to load existing key from SharedPreferences (encrypted with AndroidKeyStore)
        val prefs = context.getSharedPreferences("autologin_secure", Context.MODE_PRIVATE)
        val stored = prefs.getString("db_key", null)
        if (stored != null) {
            return Base64.decode(stored, Base64.NO_WRAP)
        }

        // Generate new random passphrase and store it
        val passphrase = ByteArray(32).apply {
            java.security.SecureRandom().nextBytes(this)
        }
        prefs.edit().putString("db_key", Base64.encodeToString(passphrase, Base64.NO_WRAP)).apply()
        return passphrase
    }
}
