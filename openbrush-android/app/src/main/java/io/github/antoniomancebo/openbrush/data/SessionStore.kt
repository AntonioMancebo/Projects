package io.github.antoniomancebo.openbrush.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class SessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("openbrush_sessions", Context.MODE_PRIVATE)

    fun load(): List<BrushSession> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    add(
                        BrushSession(
                            startedAtEpochMs = obj.getLong("startedAt"),
                            durationSeconds = obj.getInt("duration"),
                            mode = obj.optString("mode", "Desconocido"),
                            maxSector = if (obj.isNull("maxSector")) null else obj.optInt("maxSector"),
                            highPressureObserved = obj.optBoolean("highPressure", false),
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun append(session: BrushSession) {
        val updated = (listOf(session) + load()).take(MAX_SESSIONS)
        val array = JSONArray()
        updated.forEach { item ->
            array.put(JSONObject().apply {
                put("startedAt", item.startedAtEpochMs)
                put("duration", item.durationSeconds)
                put("mode", item.mode)
                put("maxSector", item.maxSector ?: JSONObject.NULL)
                put("highPressure", item.highPressureObserved)
            })
        }
        prefs.edit().putString(KEY, array.toString()).apply()
    }

    fun clear() { prefs.edit().remove(KEY).apply() }

    private companion object {
        const val KEY = "sessions"
        const val MAX_SESSIONS = 200
    }
}
