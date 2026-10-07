package com.antoniomancebo.rentabilidadinmueble

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import com.antoniomancebo.rentabilidadinmueble.ui.RentabilityApp
import com.antoniomancebo.rentabilidadinmueble.ui.SharedProductDraft

class MainActivity : ComponentActivity() {
    private val incomingShare = mutableStateOf<SharedProductDraft?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        incomingShare.value = intent.toSharedProductDraft()

        setContent {
            MaterialTheme {
                RentabilityApp(
                    incomingShare = incomingShare.value,
                    onShareConsumed = { incomingShare.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingShare.value = intent.toSharedProductDraft()
    }
}

private fun Intent.toSharedProductDraft(): SharedProductDraft? {
    if (action != Intent.ACTION_SEND || type?.startsWith("text/") != true) return null

    val sharedText = getStringExtra(Intent.EXTRA_TEXT).orEmpty()
    val subject = getStringExtra(Intent.EXTRA_SUBJECT).orEmpty()
    val url = Regex("""https?://\S+""")
        .find(sharedText)
        ?.value
        ?.trimEnd('.', ',', ';', ':', ')', ']', '}')
        ?: return null

    val title = subject.ifBlank {
        sharedText
            .replace(url, "")
            .trim()
            .lineSequence()
            .firstOrNull()
            .orEmpty()
            .take(100)
    }

    return SharedProductDraft(url = url, title = title)
}
