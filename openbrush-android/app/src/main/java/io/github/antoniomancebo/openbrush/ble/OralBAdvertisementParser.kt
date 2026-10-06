package io.github.antoniomancebo.openbrush.ble

/** Decodes Oral-B manufacturer advertisements (company ID 0x00DC / P&G). */
object OralBAdvertisementParser {
    const val ORAL_B_MANUFACTURER_ID = 0x00DC

    private val smartModes = mapOf(
        0 to "Apagado", 1 to "Limpieza diaria", 2 to "Sensible", 3 to "Cuidado de encías",
        4 to "Blanqueamiento", 5 to "Limpieza profunda", 6 to "Lengua", 7 to "Turbo", 255 to "Desconocido"
    )
    private val ioModes = mapOf(
        0 to "Limpieza diaria", 1 to "Sensible", 2 to "Cuidado de encías", 3 to "Blanqueamiento",
        4 to "Intenso", 5 to "Super sensible", 6 to "Lengua", 8 to "Ajustes", 9 to "Apagado", 11 to "Smart adapt"
    )
    private val d706Ids = setOf(112, 113, 114, 117, 118, 119)
    private val ioIds = setOf(48, 49, 50, 52, 53, 54)

    fun parse(data: ByteArray, rssi: Int = 0): BrushAdvertisement? {
        if (data.size != 9 && data.size != 11) return null
        val protocolVersion = data.u8(0)
        val modelId = data.u8(1)
        val state = decodeState(data.u8(3))
        val elapsedSeconds = data.u8(5) * 60 + data.u8(6)
        val modeId = data.u8(7)
        val sectorCount = data.getOrNull(10)?.toUByte()?.toInt()
        val modes = if (modelId in ioIds) ioModes else smartModes
        return BrushAdvertisement(
            protocolVersion = protocolVersion,
            modelId = modelId,
            modelName = modelName(modelId),
            state = state,
            pressure = decodePressure(data.u8(4)),
            elapsedSeconds = elapsedSeconds,
            modeId = modeId,
            modeName = modes[modeId] ?: "Modo $modeId",
            sector = if (state == BrushState.RUNNING) decodeSector(data.u8(8), sectorCount) else null,
            sectorTimerSeconds = data.getOrNull(9)?.toUByte()?.toInt(),
            numberOfSectors = sectorCount,
            rssi = rssi,
        )
    }

    private fun modelName(modelId: Int): String = when (modelId) {
        0, 1, 2 -> "Triumph D36"
        32, 33, 34 -> "Genius D701"
        39, 40, 41 -> "Smart/Pro D700"
        in d706Ids -> "Genius X D706"
        48, 49, 50, 54 -> "iO Series"
        52 -> "iO Series 4"
        53 -> "iO Series 5"
        64, 65, 66, 67, 68, 69, 70 -> "SmartSeries D21"
        80, 81, 82, 83, 84, 85, 86, 87 -> "Pro D601"
        else -> "Oral-B (modelo $modelId)"
    }

    private fun decodeState(value: Int): BrushState = when (value) {
        0 -> BrushState.UNKNOWN
        1 -> BrushState.INITIALIZING
        2 -> BrushState.IDLE
        3 -> BrushState.RUNNING
        4 -> BrushState.CHARGING
        5 -> BrushState.SETUP
        6 -> BrushState.FLIGHT_MENU
        8 -> BrushState.SELECTION_MENU
        9 -> BrushState.OFF
        10 -> BrushState.POST_BRUSHING
        115 -> BrushState.SLEEPING
        116 -> BrushState.TRANSPORT
        else -> BrushState.OTHER
    }

    private fun decodePressure(value: Int): BrushPressure = when {
        value and 0x08 != 0 -> BrushPressure.POWER_BUTTON
        value and 0x04 != 0 -> BrushPressure.BUTTON
        value and 0x80 != 0 -> BrushPressure.HIGH
        else -> BrushPressure.NORMAL
    }

    private fun decodeSector(raw: Int, sectorCount: Int?): Int? = when (val quadrant = raw and 0x07) {
        0 -> null
        7 -> ((sectorCount ?: 4) and 0x07).takeIf { it > 0 } ?: 4
        else -> quadrant
    }

    private fun ByteArray.u8(index: Int): Int = this[index].toUByte().toInt()
}
