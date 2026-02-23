package io.github.lugf027.apng

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@InternalApngApi
public actual fun Apng.ioDispatcher(): CoroutineDispatcher = Dispatchers.IO
