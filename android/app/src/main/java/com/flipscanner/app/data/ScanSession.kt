package com.flipscanner.app.data

import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-memory holder for the latest scan's photos (1–3) while moving
 * between the scan and result screens.
 */
@Singleton
class ScanSession @Inject constructor() {

    /** Absolute file paths of captured/imported photos, max 3. */
    val photoPaths: MutableList<String> = mutableListOf()

    /** Backward-compat: the most recently added photo. */
    val lastPhotoPath: String?
        get() = photoPaths.lastOrNull()

    fun clearPhotos() {
        photoPaths.clear()
    }
}
