package com.example.kidtubelock.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.kidtubelock.domain.model.ApprovedMediaSelectionResolver
import com.example.kidtubelock.domain.model.AppSettings
import com.example.kidtubelock.domain.model.ApprovedMediaItem
import com.example.kidtubelock.domain.repository.ParentControlsRepository
import com.example.kidtubelock.domain.security.PinHasher
import com.example.kidtubelock.feature_lock.LockMode
import com.example.kidtubelock.feature_lock.LockModePreferenceMapper
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Singleton
class DataStoreParentControlsRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val json: Json,
) : ParentControlsRepository {
    override val appSettings: Flow<AppSettings> = dataStore.data
        .catch { throwable ->
            if (throwable is IOException) {
                emit(emptyPreferences())
            } else {
                throw throwable
            }
        }
        .map { preferences ->
            val approvedMedia = decodeApprovedMedia(
                preferences[APPROVED_MEDIA_KEY].orEmpty(),
            )
            val selectedApprovedMediaItemId = ApprovedMediaSelectionResolver.resolveSelectedItemId(
                approvedMedia = approvedMedia,
                requestedSelectedItemId = preferences[SELECTED_APPROVED_MEDIA_ITEM_ID_KEY],
            )
            val preferredLockMode = LockModePreferenceMapper.decode(
                preferences[PREFERRED_LOCK_MODE_KEY],
            )

            AppSettings(
                hasParentPin = !preferences[PARENT_PIN_SALT_KEY].isNullOrBlank() &&
                    !preferences[PARENT_PIN_HASH_KEY].isNullOrBlank(),
                approvedMedia = approvedMedia,
                selectedApprovedMediaItemId = selectedApprovedMediaItemId,
                preferredLockMode = preferredLockMode,
                timeLimitMinutes = preferences[TIME_LIMIT_MINUTES_KEY],
            )
        }

    override suspend fun saveParentPin(pin: String) {
        val storedPinHash = PinHasher.create(pin)
        dataStore.edit { preferences ->
            preferences[PARENT_PIN_SALT_KEY] = storedPinHash.saltBase64
            preferences[PARENT_PIN_HASH_KEY] = storedPinHash.hashBase64
        }
    }

    override suspend fun verifyParentPin(pin: String): Boolean {
        val settings = dataStore.data.map { preferences ->
            PinHasher.verify(
                pin = pin,
                saltBase64 = preferences[PARENT_PIN_SALT_KEY].orEmpty(),
                hashBase64 = preferences[PARENT_PIN_HASH_KEY].orEmpty(),
            )
        }
        return settings.firstOrNull() ?: false
    }

    override suspend fun addApprovedMedia(item: ApprovedMediaItem) {
        dataStore.edit { preferences ->
            val currentItems = decodeApprovedMedia(preferences[APPROVED_MEDIA_KEY].orEmpty())
            val updatedItems = currentItems + item
            val selectedItemId = ApprovedMediaSelectionResolver.resolveSelectedItemId(
                approvedMedia = updatedItems,
                requestedSelectedItemId = preferences[SELECTED_APPROVED_MEDIA_ITEM_ID_KEY],
            )

            preferences[APPROVED_MEDIA_KEY] = json.encodeToString(
                ListSerializer(ApprovedMediaItem.serializer()),
                updatedItems,
            )
            updateSelectedItemPreference(preferences, selectedItemId)
        }
    }

    override suspend fun updateApprovedMedia(item: ApprovedMediaItem) {
        dataStore.edit { preferences ->
            val currentItems = decodeApprovedMedia(preferences[APPROVED_MEDIA_KEY].orEmpty())
            val updatedItems = currentItems.map { existing ->
                if (existing.localId == item.localId) item else existing
            }
            val selectedItemId = ApprovedMediaSelectionResolver.resolveSelectedItemId(
                approvedMedia = updatedItems,
                requestedSelectedItemId = preferences[SELECTED_APPROVED_MEDIA_ITEM_ID_KEY],
            )

            preferences[APPROVED_MEDIA_KEY] = json.encodeToString(
                ListSerializer(ApprovedMediaItem.serializer()),
                updatedItems,
            )
            updateSelectedItemPreference(preferences, selectedItemId)
        }
    }

    override suspend fun deleteApprovedMedia(itemId: String) {
        dataStore.edit { preferences ->
            val currentItems = decodeApprovedMedia(preferences[APPROVED_MEDIA_KEY].orEmpty())
            val updatedItems = currentItems.filterNot { it.localId == itemId }
            val currentSelection = preferences[SELECTED_APPROVED_MEDIA_ITEM_ID_KEY]
            val requestedSelection = currentSelection.takeUnless { it == itemId }
            val selectedItemId = ApprovedMediaSelectionResolver.resolveSelectedItemId(
                approvedMedia = updatedItems,
                requestedSelectedItemId = requestedSelection,
            )

            preferences[APPROVED_MEDIA_KEY] = json.encodeToString(
                ListSerializer(ApprovedMediaItem.serializer()),
                updatedItems,
            )
            updateSelectedItemPreference(preferences, selectedItemId)
        }
    }

    override suspend fun selectApprovedMedia(itemId: String?) {
        dataStore.edit { preferences ->
            val currentItems = decodeApprovedMedia(preferences[APPROVED_MEDIA_KEY].orEmpty())
            val selectedItemId = ApprovedMediaSelectionResolver.resolveSelectedItemId(
                approvedMedia = currentItems,
                requestedSelectedItemId = itemId,
            )
            updateSelectedItemPreference(preferences, selectedItemId)
        }
    }

    override suspend fun setPreferredLockMode(lockMode: LockMode) {
        dataStore.edit { preferences ->
            preferences[PREFERRED_LOCK_MODE_KEY] = LockModePreferenceMapper.encode(lockMode)
        }
    }

    override suspend fun setTimeLimitMinutes(minutes: Int?) {
        dataStore.edit { preferences ->
            if (minutes == null) {
                preferences.remove(TIME_LIMIT_MINUTES_KEY)
            } else {
                preferences[TIME_LIMIT_MINUTES_KEY] = minutes
            }
        }
    }

    override fun approvedMediaItem(itemId: String): Flow<ApprovedMediaItem?> = appSettings
        .map { settings ->
            settings.approvedMedia.firstOrNull { it.localId == itemId }
        }

    private fun decodeApprovedMedia(encoded: String): List<ApprovedMediaItem> {
        if (encoded.isBlank()) {
            return emptyList()
        }

        return runCatching {
            json.decodeFromString(
                ListSerializer(ApprovedMediaItem.serializer()),
                encoded,
            )
        }.getOrDefault(emptyList())
    }

    private fun updateSelectedItemPreference(
        preferences: MutablePreferences,
        selectedItemId: String?,
    ) {
        if (selectedItemId == null) {
            preferences.remove(SELECTED_APPROVED_MEDIA_ITEM_ID_KEY)
        } else {
            preferences[SELECTED_APPROVED_MEDIA_ITEM_ID_KEY] = selectedItemId
        }
    }

    private companion object {
        val PARENT_PIN_SALT_KEY = stringPreferencesKey("parent_pin_salt")
        val PARENT_PIN_HASH_KEY = stringPreferencesKey("parent_pin_hash")
        val APPROVED_MEDIA_KEY = stringPreferencesKey("approved_media_json")
        val SELECTED_APPROVED_MEDIA_ITEM_ID_KEY =
            stringPreferencesKey("selected_approved_media_item_id")
        val PREFERRED_LOCK_MODE_KEY = stringPreferencesKey("preferred_lock_mode")
        val TIME_LIMIT_MINUTES_KEY = intPreferencesKey("time_limit_minutes")
    }
}
