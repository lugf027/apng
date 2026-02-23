package io.github.lugf027.apng

import okio.FileHandle
import okio.FileMetadata
import okio.FileSystem
import okio.Path
import okio.Sink
import okio.Source

@InternalApngApi
public actual fun defaultFileSystem(): FileSystem = ThrowingFileSystem

private object ThrowingFileSystem : FileSystem() {
    override fun atomicMove(source: Path, target: Path) = throwUnsupported()
    override fun canonicalize(path: Path): Path = throwUnsupported()
    override fun createDirectory(dir: Path, mustCreate: Boolean) = throwUnsupported()
    override fun createSymlink(source: Path, target: Path) = throwUnsupported()
    override fun delete(path: Path, mustExist: Boolean) = throwUnsupported()
    override fun list(dir: Path): List<Path> = throwUnsupported()
    override fun listOrNull(dir: Path): List<Path>? = throwUnsupported()
    override fun metadataOrNull(path: Path): FileMetadata? = throwUnsupported()
    override fun openReadOnly(file: Path): FileHandle = throwUnsupported()
    override fun openReadWrite(file: Path, mustCreate: Boolean, mustExist: Boolean): FileHandle = throwUnsupported()
    override fun sink(file: Path, mustCreate: Boolean): Sink = throwUnsupported()
    override fun appendingSink(file: Path, mustExist: Boolean): Sink = throwUnsupported()
    override fun source(file: Path): Source = throwUnsupported()

    @OptIn(InternalApngApi::class)
    private fun throwUnsupported(): Nothing {
        throw UnsupportedFileSystemException()
    }
}
