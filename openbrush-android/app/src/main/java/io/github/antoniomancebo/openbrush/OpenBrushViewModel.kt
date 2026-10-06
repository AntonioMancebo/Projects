package io.github.antoniomancebo.openbrush

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.github.antoniomancebo.openbrush.ble.BrushAdvertisement
import io.github.antoniomancebo.openbrush.ble.BrushPressure
import io.github.antoniomancebo.openbrush.ble.OralBBleScanner
import io.github.antoniomancebo.openbrush.data.BrushSession
import io.github.antoniomancebo.openbrush.data.SessionStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class OpenBrushUiState(
    val scanning: Boolean = false,
    val lastAdvertisement: BrushAdvertisement? = null,
    val error: String? = null,
    val sessions: List<BrushSession> = emptyList(),
)

class OpenBrushViewModel(application: Application) : AndroidViewModel(application) {
    private val store = SessionStore(application)
    private val _uiState = MutableStateFlow(OpenBrushUiState(sessions = store.load()))
    val uiState: StateFlow<OpenBrushUiState> = _uiState.asStateFlow()

    private var activeStartedAt: Long? = null
    private var activeMode: String = "Desconocido"
    private var maxSector: Int? = null
    private var sawHighPressure = false
    private var wasBrushing = false
    private var lastRunningDuration = 0

    private val scanner = OralBBleScanner(
        context = application,
        onAdvertisement = ::onAdvertisement,
        onError = { message -> _uiState.update { it.copy(scanning = false, error = message) } },
    )

    fun startScanning() {
        _uiState.update { it.copy(scanning = true, error = null) }
        scanner.start()
    }

    fun stopScanning() {
        scanner.stop()
        _uiState.update { it.copy(scanning = false) }
    }

    fun clearHistory() {
        store.clear()
        _uiState.update { it.copy(sessions = emptyList()) }
    }

    private fun onAdvertisement(ad: BrushAdvertisement) {
        val nowBrushing = ad.isBrushing

        if (nowBrushing && !wasBrushing) {
            activeStartedAt = System.currentTimeMillis() - ad.elapsedSeconds * 1000L
            activeMode = ad.modeName
            maxSector = ad.sector
            sawHighPressure = ad.pressure == BrushPressure.HIGH
            lastRunningDuration = ad.elapsedSeconds
        } else if (nowBrushing) {
            activeMode = ad.modeName
            lastRunningDuration = maxOf(lastRunningDuration, ad.elapsedSeconds)
            ad.sector?.let { sector -> maxSector = maxOf(maxSector ?: sector, sector) }
            if (ad.pressure == BrushPressure.HIGH) sawHighPressure = true
        } else if (!nowBrushing && wasBrushing) {
            val start = activeStartedAt
            if (start != null && lastRunningDuration >= 10) {
                store.append(
                    BrushSession(
                        startedAtEpochMs = start,
                        durationSeconds = lastRunningDuration,
                        mode = activeMode,
                        maxSector = maxSector,
                        highPressureObserved = sawHighPressure,
                    )
                )
                _uiState.update { it.copy(sessions = store.load()) }
            }
            activeStartedAt = null
            maxSector = null
            sawHighPressure = false
            lastRunningDuration = 0
        }

        wasBrushing = nowBrushing
        _uiState.update { it.copy(lastAdvertisement = ad, error = null) }
    }

    override fun onCleared() {
        scanner.stop()
        super.onCleared()
    }
}
