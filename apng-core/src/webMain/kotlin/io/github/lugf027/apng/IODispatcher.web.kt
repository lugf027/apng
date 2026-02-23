package io.github.lugf027.apng

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@InternalApngApi
public actual fun Apng.ioDispatcher(): CoroutineDispatcher = Dispatchers.Default
