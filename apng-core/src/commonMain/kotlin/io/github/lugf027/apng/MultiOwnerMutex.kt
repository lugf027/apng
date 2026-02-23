package io.github.lugf027.apng

import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@InternalApngApi
public class MultiOwnerMutex {

    private val lock = reentrantLock()
    private val mutexes = mutableMapOf<Any, MutexEntry>()

    public suspend fun <T> withLock(key: Any, action: suspend () -> T): T {
        val entry = lock.withLock {
            mutexes.getOrPut(key) { MutexEntry(Mutex(), 0) }.also { it.waiters++ }
        }

        return try {
            entry.mutex.withLock {
                action()
            }
        } finally {
            lock.withLock {
                entry.waiters--
                if (entry.waiters == 0) {
                    mutexes.remove(key)
                }
            }
        }
    }

    private class MutexEntry(val mutex: Mutex, var waiters: Int)
}
