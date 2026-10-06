package io.github.antoniomancebo.openbrush.data

data class BrushSession(
    val startedAtEpochMs: Long,
    val durationSeconds: Int,
    val mode: String,
    val maxSector: Int?,
    val highPressureObserved: Boolean,
)
