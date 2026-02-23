package io.github.lugf027.apng

import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import androidx.compose.runtime.State

@Stable
public interface ApngCompositionResult : State<ApngComposition?> {

    override val value: ApngComposition?

    public val error: Throwable?

    public val isLoading: Boolean

    public val isComplete: Boolean

    public val isFailure: Boolean

    public val isSuccess: Boolean

    public suspend fun await(): ApngComposition
}

@Stable
internal class ApngCompositionResultImpl : ApngCompositionResult {

    private var compositionDeferred = CompletableDeferred<ApngComposition>()

    override var value: ApngComposition? by mutableStateOf(null)
        private set

    override var error by mutableStateOf<Throwable?>(null)
        private set

    override val isLoading by derivedStateOf { value == null && error == null }

    override val isComplete by derivedStateOf { value != null || error != null }

    override val isFailure by derivedStateOf { error != null }

    override val isSuccess by derivedStateOf { value != null }

    override suspend fun await(): ApngComposition {
        return compositionDeferred.await()
    }

    private val mutex = Mutex()

    internal suspend fun complete(composition: ApngComposition) {
        mutex.withLock {
            if (isComplete) return@withLock
            value = composition
            compositionDeferred.complete(composition)
        }
    }

    internal suspend fun completeExceptionally(error: Throwable) {
        mutex.withLock {
            if (isComplete) return@withLock
            this.error = error
            compositionDeferred.completeExceptionally(error)
        }
    }
}
