package com.flipscanner.app.data

import javax.inject.Inject
import javax.inject.Singleton

/** In-memory holder for the latest scan while moving between screens. */
@Singleton
class ScanSession @Inject constructor() {
    var lastPhotoPath: String? = null
}
