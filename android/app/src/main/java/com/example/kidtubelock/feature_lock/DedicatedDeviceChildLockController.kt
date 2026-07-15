package com.example.kidtubelock.feature_lock

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DedicatedDeviceChildLockController @Inject constructor() : ChildLockController {
    override val lockMode: LockMode = LockMode.DEDICATED_DEVICE

    override fun launchSupport(): ChildLockLaunchSupport {
        return ChildLockLaunchSupport(
            summary = "Dedicated-device preview mode prepares the app for future Lock Task Mode " +
                "or device-owner setup. This build does not have privileged enforcement yet, so " +
                "it still relies on ordinary app-level protections.",
            screenPinningTitle = "Dedicated-device preview checklist",
            screenPinningSteps = listOf(
                "Treat this as a future dedicated-device path, not a fully locked kiosk mode yet.",
                "Use a phone or tablet you can manage as a family device if you want to test this mode.",
                "Keep screen pinning available as a fallback until real Lock Task Mode support exists.",
                "Future work should connect this mode to Android device-owner or Lock Task setup.",
            ),
            inSessionHint = "Dedicated-device preview mode is active. Real Lock Task enforcement is not wired yet, so parent unlock and normal Android limitations still apply.",
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

