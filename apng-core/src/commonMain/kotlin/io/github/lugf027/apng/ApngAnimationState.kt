package io.github.lugf027.apng

import androidx.compose.animation.core.AnimationConstants
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State

@Stable
public interface ApngAnimationState : State<Float> {
    public val isPlaying: Boolean

    public val progress: Float

    public val iteration: Int

    public val iterations: Int

    public val reverseOnRepeat: Boolean

    public val clipSpec: ApngClipSpec?

    public val speed: Float

    public val useCompositionFrameRate: Boolean

    public val composition: ApngComposition?

    public val lastFrameNanos: Long get() = AnimationConstants.UnspecifiedTime

    public val isAtEnd: Boolean
}
