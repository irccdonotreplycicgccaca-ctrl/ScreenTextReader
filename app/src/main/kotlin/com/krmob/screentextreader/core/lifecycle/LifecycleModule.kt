package com.krmob.screentextreader.core.lifecycle

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface LifecycleModule {
    @Binds
    fun bindAppVisibility(appVisibility: ProcessLifecycleAppVisibility): AppVisibility
}
