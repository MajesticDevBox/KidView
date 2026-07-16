package com.mdev.kidview.feature_lock

import org.junit.Assert.assertEquals
import org.junit.Test

class LockModePreferenceMapperTest {
    @Test
    fun decode_defaultsToStandardPhoneForUnknownValues() {
        assertEquals(LockMode.STANDARD_PHONE, LockModePreferenceMapper.decode(null))
        assertEquals(LockMode.STANDARD_PHONE, LockModePreferenceMapper.decode("UNKNOWN"))
    }

    @Test
    fun decode_readsKnownEnumValue() {
        assertEquals(
            LockMode.DEDICATED_DEVICE,
            LockModePreferenceMapper.decode("DEDICATED_DEVICE"),
        )
    }

    @Test
    fun encode_returnsEnumName() {
        assertEquals(
            "STANDARD_PHONE",
            LockModePreferenceMapper.encode(LockMode.STANDARD_PHONE),
        )
    }
}
