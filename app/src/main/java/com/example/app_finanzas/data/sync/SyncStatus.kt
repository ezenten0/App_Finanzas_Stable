package com.example.app_finanzas.data.sync

/**
 * Enum that describes the synchronization state of a local entity. It is used to
 * orchestrate offline-first CRUD operations and replay them once connectivity
 * is restored.
 */
enum class SyncStatus {
    SYNCED,
    PENDING_UPLOAD,
    PENDING_DELETE
}
