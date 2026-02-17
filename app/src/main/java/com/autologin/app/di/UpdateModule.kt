package com.autologin.app.di

import com.autologin.app.data.repository.GithubUpdateRepository
import com.autologin.app.domain.repository.UpdateRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UpdateModule {

    @Binds
    @Singleton
    abstract fun bindUpdateRepository(impl: GithubUpdateRepository): UpdateRepository
}
