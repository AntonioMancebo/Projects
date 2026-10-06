package io.github.antoniomancebo.openbrush.ble

enum class BrushState(val label: String) {
    UNKNOWN("Desconocido"), INITIALIZING("Iniciando"), IDLE("En reposo"), RUNNING("Cepillando"),
    CHARGING("Cargando"), SETUP("Configuración"), FLIGHT_MENU("Modo vuelo"),
    SELECTION_MENU("Selección"), OFF("Apagado"), POST_BRUSHING("Resumen"),
    SLEEPING("Dormido"), TRANSPORT("Transporte"), OTHER("Otro")
}

enum class BrushPressure(val label: String) {
    NORMAL("Correcta"), HIGH("Demasiada"), BUTTON("Botón pulsado"), POWER_BUTTON("Botón de encendido")
}

data class BrushAdvertisement(
    val protocolVersion: Int,
    val modelId: Int,
    val modelName: String,
    val state: BrushState,
    val pressure: BrushPressure,
    val elapsedSeconds: Int,
    val modeId: Int,
    val modeName: String,
    val sector: Int?,
    val sectorTimerSeconds: Int?,
    val numberOfSectors: Int?,
    val rssi: Int,
) {
    val isBrushing: Boolean get() = state == BrushState.RUNNING
}
