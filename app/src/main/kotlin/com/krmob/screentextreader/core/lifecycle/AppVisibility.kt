package com.krmob.screentextreader.core.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Tells whether any screen of this app is visible to the user. */
interface AppVisibility {
    val isInForeground: Flow<Boolean>
}

class ProcessLifecycleAppVisibility @Inject constructor() : AppVisibility {
    override val isInForeground: Flow<Boolean> =
        ProcessLifecycleOwner.get().lifecycle.currentStateFlow
            .map { state -> state.isAtLeast(Lifecycle.State.STARTED) }
            .distinctUntilChanged()
}
