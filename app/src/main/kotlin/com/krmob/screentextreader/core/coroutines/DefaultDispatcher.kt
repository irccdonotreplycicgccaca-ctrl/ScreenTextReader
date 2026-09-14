package com.krmob.screentextreader.core.coroutines

import javax.inject.Qualifier

/** Dispatcher for CPU-bound work: frame copies, text recognition and text analysis. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DefaultDispatcher
