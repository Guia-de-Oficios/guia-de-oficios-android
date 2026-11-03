package ar.guiaoficios.app.data.api

import android.content.Context
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ar.guiaoficios.app.BuildConfig
import ar.guiaoficios.app.data.model.UserDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "guia_oficios_session")

/**
 * Gestiona la persistencia real de sesión JWT, datos del vecino/profesional y URL del servidor (GDO-20).
 */
class SessionManager(private val context: Context) {

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("jwt_token")
        private val KEY_USER_ID = stringPreferencesKey("user_id")
        private val KEY_FULL_NAME = stringPreferencesKey("full_name")
        private val KEY_EMAIL = stringPreferencesKey("email")
        private val KEY_PHONE = stringPreferencesKey("phone")
        private val KEY_ROLE = stringPreferencesKey("role")
        private val KEY_ADDRESS = stringPreferencesKey("saved_address")
        private val KEY_LAT = doublePreferencesKey("saved_lat")
        private val KEY_LNG = doublePreferencesKey("saved_lng")
        private val KEY_PRO_ID = stringPreferencesKey("professional_id")
        private val KEY_API_URL = stringPreferencesKey("api_base_url")
    }

    @Volatile
    var cachedToken: String? = null
        private set

    @Volatile
    var cachedBaseUrl: String = BuildConfig.API_BASE_URL
        private set

    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        val token = prefs[KEY_TOKEN]
        cachedToken = token
        token
    }

    val userFlow: Flow<UserDto?> = context.dataStore.data.map { prefs ->
        val id = prefs[KEY_USER_ID] ?: return@map null
        val token = prefs[KEY_TOKEN]
        if (token.isNullOrBlank()) return@map null
        UserDto(
            id = id,
            full_name = prefs[KEY_FULL_NAME] ?: "",
            email = prefs[KEY_EMAIL] ?: "",
            phone = prefs[KEY_PHONE] ?: "",
            role = prefs[KEY_ROLE] ?: "client",
            saved_address = prefs[KEY_ADDRESS] ?: "Av. Rivadavia 5420, Caballito",
            saved_lat = prefs[KEY_LAT] ?: -34.6186,
            saved_lng = prefs[KEY_LNG] ?: -58.4368,
            professional_id = prefs[KEY_PRO_ID]
        )
    }

    suspend fun saveAuthSession(token: String, user: UserDto) {
        cachedToken = token
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_USER_ID] = user.id
            prefs[KEY_FULL_NAME] = user.full_name
            prefs[KEY_EMAIL] = user.email
            prefs[KEY_PHONE] = user.phone
            prefs[KEY_ROLE] = user.role
            prefs[KEY_ADDRESS] = user.saved_address
            prefs[KEY_LAT] = user.saved_lat
            prefs[KEY_LNG] = user.saved_lng
            if (!user.professional_id.isNullOrBlank()) {
                prefs[KEY_PRO_ID] = user.professional_id
            }
        }
    }

    suspend fun updateSavedUser(user: UserDto) {
        context.dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = user.id
            prefs[KEY_FULL_NAME] = user.full_name
            prefs[KEY_EMAIL] = user.email
            prefs[KEY_PHONE] = user.phone
            prefs[KEY_ROLE] = user.role
            prefs[KEY_ADDRESS] = user.saved_address
            prefs[KEY_LAT] = user.saved_lat
            prefs[KEY_LNG] = user.saved_lng
            if (!user.professional_id.isNullOrBlank()) {
                prefs[KEY_PRO_ID] = user.professional_id
            }
        }
    }

    suspend fun setCustomApiUrl(url: String) {
        val normalized = if (url.endsWith("/")) url else "$url/"
        cachedBaseUrl = normalized
        context.dataStore.edit { prefs ->
            prefs[KEY_API_URL] = normalized
        }
    }

    suspend fun clearSession() {
        cachedToken = null
        context.dataStore.edit { it.clear() }
    }
}
