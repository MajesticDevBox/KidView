package com.mdev.kidview.feature_lock

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StandardPhoneChildLockController @Inject constructor() : ChildLockController {
    override val lockMode: LockMode = LockMode.STANDARD_PHONE

    override fun launchSupport(): ChildLockLaunchSupport {
        return ChildLockLaunchSupport(
            summary = "Standard phone mode uses immersive mode plus parent-guided screen pinning. " +
                "A future dedicated-device mode can replace this with stronger lock behavior.",
            screenPinningTitle = "Before handing over the phone",
            screenPinningSteps = listOf(
                "Turn on Android screen pinning in system settings if it is not enabled yet.",
                "Open the selected video in child mode from this app.",
                "Use Android's Recents overview and start screen pinning for KidView Lock.",
                "Use the parent unlock flow in the app before unpinning or exiting child mode.",
            ),
            inSessionHint = "Parent unlock is still available here, but screen pinning should be started manually for better containment.",
        )
    }

    override fun sessionPolicy(): ChildSessionPolicy {
        return ChildSessionPolicy(
            keepScreenAwake = true,
            useImmersiveMode = true,
            absorbBackPress = true,
        )
    }
}
