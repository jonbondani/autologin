package com.autologin.app.di

import android.content.Context
import androidx.room.Room
import com.autologin.app.data.local.AuthEventDao
import com.autologin.app.data.local.AuthEventDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AuthEventDatabase {
        return Room.databaseBuilder(
            context,
            AuthEventDatabase::class.java,
            "autologin.db",
        ).build()
    }

    @Provides
    fun provideAuthEventDao(database: AuthEventDatabase): AuthEventDao {
        return database.authEventDao()
    }
}
