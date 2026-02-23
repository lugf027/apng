package io.github.lugf027.apng

import okio.Closeable
import okio.FileNotFoundException
import okio.FileSystem
import okio.IOException
import okio.Path
import kotlin.random.Random
import kotlin.random.nextULong

@InternalApngApi
public expect fun defaultFileSystem(): FileSystem

@InternalApngApi
public class UnsupportedFileSystemException : ApngException("File system is not supported")

@InternalApngApi
public fun Closeable.closeQuietly() {
    try {
        close()
    } catch (e: RuntimeException) {
        throw e
    } catch (_: Exception) {}
}

@InternalApngApi
public fun FileSystem.createFile(file: Path, mustCreate: Boolean = false) {
    if (mustCreate) {
        sink(file, mustCreate = true).closeQuietly()
    } else if (!exists(file)) {
        sink(file).closeQuietly()
    }
}

@InternalApngApi
public fun FileSystem.deleteContents(directory: Path) {
    var exception: IOException? = null
    val files = try {
        list(directory)
    } catch (_: FileNotFoundException) {
        return
    }
    for (file in files) {
        try {
            if (metadata(file).isDirectory) {
                deleteContents(file)
            }
            delete(file)
        } catch (e: IOException) {
            if (exception == null) {
                exception = e
            }
        }
    }
    if (exception != null) {
        throw exception
    }
}
