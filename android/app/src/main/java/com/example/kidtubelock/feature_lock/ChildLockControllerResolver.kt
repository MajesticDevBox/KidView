package com.example.kidtubelock.feature_lock

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChildLockControllerResolver @Inject constructor(
    private val standardPhoneChildLockController: StandardPhoneChildLockController,
    private val dedicatedDeviceChildLockController: DedicatedDeviceChildLockController,
) {
    fun controllerFor(lockMode: LockMode): ChildLockController {
        return when (lockMode) {
            LockMode.STANDARD_PHONE -> standardPhoneChildLockController
            LockMode.DEDICATED_DEVICE -> dedicatedDeviceChildLockController
        }
    }
}
