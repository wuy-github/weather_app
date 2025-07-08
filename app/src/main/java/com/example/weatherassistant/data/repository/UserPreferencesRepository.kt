package com.example.weatherassistant.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Khởi tạo DataStore với một tên duy nhất
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class UserPreferencesRepository(private val context: Context) {

    // Đây là "chìa khóa" để chúng ta tìm đến đúng dữ liệu cần lưu
    private object PreferencesKeys {
        val SEARCH_HISTORY = stringSetPreferencesKey("search_history")
    }

    // Tạo một luồng (Flow) để đọc lịch sử tìm kiếm.
    // Giao diện sẽ lắng nghe luồng này để tự động cập nhật khi có thay đổi.
    val searchHistoryFlow: Flow<Set<String>> = context.dataStore.data
        .map { preferences ->
            // Đọc dữ liệu từ key, nếu không có thì trả về một danh sách rỗng
            preferences[PreferencesKeys.SEARCH_HISTORY] ?: emptySet()
        }

    // Hàm để thêm một địa điểm mới vào lịch sử
    suspend fun addLocationToHistory(location: String) {
        context.dataStore.edit { preferences ->
            // Lấy danh sách lịch sử hiện tại
            val currentHistory = preferences[PreferencesKeys.SEARCH_HISTORY] ?: emptySet()
            // Thêm địa điểm mới vào và lưu lại
            preferences[PreferencesKeys.SEARCH_HISTORY] = currentHistory + location
        }
    }
    //xóa lịch sử tìm kiếm
    suspend fun clearHistory() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.SEARCH_HISTORY)
        }
    }
}