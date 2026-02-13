package com.autologin.app.di

import com.autologin.app.data.repository.MsalAuthRepository
import com.autologin.app.data.repository.RoomHistoryRepository
import com.autologin.app.domain.repository.AuthRepository
import com.autologin.app.domain.repository.HistoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: MsalAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(impl: RoomHistoryRepository): HistoryRepository
}
