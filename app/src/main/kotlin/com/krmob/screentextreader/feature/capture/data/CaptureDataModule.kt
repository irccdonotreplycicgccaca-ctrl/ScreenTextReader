package com.krmob.screentextreader.feature.capture.data

import com.krmob.screentextreader.feature.capture.domain.CaptureSession
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface CaptureDataModule {
    @Binds
    fun bindCaptureSession(store: CaptureSessionStore): CaptureSession
}
