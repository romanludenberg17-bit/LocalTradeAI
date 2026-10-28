package com.romanludenberg.localtradeai.capture

/**
 * Fraction-based screen crop profile used before OCR.
 *
 * Fractions are normalized to the full captured frame: 0f = start of an axis,
 * 1f = end of an axis. Profiles intentionally describe UI geometry only;
 * chart/candle detection is a later stage.
 */
data class CropProfile(
    val name: String,
    val topFraction: Float,
    val bottomFraction: Float,
    val leftFraction: Float,
    val rightFraction: Float
) {
    init {
        require(topFraction in 0f..1f) { "topFraction must be in 0..1" }
        require(bottomFraction in 0f..1f) { "bottomFraction must be in 0..1" }
        require(leftFraction in 0f..1f) { "leftFraction must be in 0..1" }
        require(rightFraction in 0f..1f) { "rightFraction must be in 0..1" }
        require(topFraction < bottomFraction) { "topFraction must be below bottomFraction" }
        require(leftFraction < rightFraction) { "leftFraction must be below rightFraction" }
    }

    companion object {
        /** Existing generic behavior: keep the middle 80% vertically. */
        val GENERIC = CropProfile(
            name = "generic",
            topFraction = 0.10f,
            bottomFraction = 0.90f,
            leftFraction = 0f,
            rightFraction = 1f
        )

        /**
         * First-pass Pocket Option profile for the current mobile layout.
         * These values are deliberately a calibration starting point, not
         * hard-coded knowledge of every Pocket Option device/layout.
         */
        val POCKET_OPTION = CropProfile(
            name = "pocket_option",
            topFraction = 0.12f,
            bottomFraction = 0.72f,
            leftFraction = 0f,
            rightFraction = 1f
        )
    }
}
