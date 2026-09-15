package com.krmob.screentextreader.feature.capture.service

import android.content.Context
import android.media.projection.MediaProjectionManager
import androidx.core.content.getSystemService
import com.krmob.screentextreader.feature.capture.domain.ScreenCaptureController
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface CaptureServiceModule {
    @Binds
    fun bindScreenCaptureController(controller: ForegroundServiceCaptureController): ScreenCaptureController

    companion object {
        @Provides
        fun provideMediaProjectionManager(@ApplicationContext context: Context): MediaProjectionManager =
            checkNotNull(context.getSystemService()) { "MediaProjectionManager is not available on this device" }
    }
}
