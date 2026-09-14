package com.krmob.screentextreader.feature.capture.service

import com.krmob.screentextreader.feature.capture.domain.ScreenCaptureController
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface CaptureServiceModule {
    @Binds
    fun bindScreenCaptureController(controller: ForegroundServiceCaptureController): ScreenCaptureController
}
