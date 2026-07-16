package com.mdev.kidview.feature_lock

data class ChildLockLaunchSupport(
    val summary: String,
    val screenPinningTitle: String,
    val screenPinningSteps: List<String>,
    val inSessionHint: String,
)

data class ChildSessionPolicy(
    val keepScreenAwake: Boolean = true,
    val useImmersiveMode: Boolean = true,
    val absorbBackPress: Boolean = true,
)

interface ChildLockController {
    val lockMode: LockMode

    fun launchSupport(): ChildLockLaunchSupport

    fun sessionPolicy(): ChildSessionPolicy
}
