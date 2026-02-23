package io.github.lugf027.apng

import okio.FileSystem
import okio.SYSTEM

@InternalApngApi
public actual fun defaultFileSystem(): FileSystem = FileSystem.SYSTEM
