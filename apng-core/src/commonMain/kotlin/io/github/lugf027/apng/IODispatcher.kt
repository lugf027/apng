package io.github.lugf027.apng

import kotlinx.coroutines.CoroutineDispatcher

@InternalApngApi
public expect fun Apng.ioDispatcher(): CoroutineDispatcher
