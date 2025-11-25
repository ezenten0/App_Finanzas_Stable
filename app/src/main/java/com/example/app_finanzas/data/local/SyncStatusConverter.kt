package com.example.app_finanzas.data.local

import androidx.room.TypeConverter
import com.example.app_finanzas.data.sync.SyncStatus

/**
 * Persists [SyncStatus] enums as strings in Room so that repositories can track
 * pending operations and replay them when connectivity is back.
 */
class SyncStatusConverter {
    @TypeConverter
    fun fromStorage(value: String?): SyncStatus {
        return value?.let { runCatching { SyncStatus.valueOf(it) }.getOrNull() }
            ?: SyncStatus.SYNCED
    }

    @TypeConverter
    fun toStorage(status: SyncStatus?): String {
        return status?.name ?: SyncStatus.SYNCED.name
    }
}
