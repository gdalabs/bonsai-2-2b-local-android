package org.gdalabs.bonsailocal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import dev.gdalabs.localui.*
import org.gdalabs.bonsailocal.model.ModelManifest

class MainActivity : ComponentActivity() {
    private lateinit var model: ChatViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        model = androidx.lifecycle.ViewModelProvider(this)[ChatViewModel::class.java]
        setContent {
            val state by model.state.collectAsState()
            val options = androidx.compose.runtime.remember { model.repository.officialManifests() }
            val s = state.settings
            ModelAppTheme {
                ModelAppScreen(AppIdentity("Bonsai 2 2B", "Local", BuildConfig.VERSION_NAME, "bonsai-Q2_0.json", "Apache-2.0.txt", "Apache-2.0.txt", "Q2_0"),
                    ModelAppState(state.messages.map { UiMessage(it.role, it.content, it.reasoning) },
                        state.models.map { UiModel(it.manifest.sha256, it.manifest.name, it.manifest.format, it.manifest.size) },
                        state.ready, state.busy, state.generating, state.selected, state.status, state.error, state.information, state.stats,
                        UiTuning(s.threads, s.context, s.maxTokens, s.temperature, s.topP, s.topK, s.thinking, s.memoryEfficient, false)),
                    ModelAppActions(model::send, model::stop, model::reset,
                        { id -> state.models.firstOrNull { it.manifest.sha256 == id }?.let(model::load) }, model::unload,
                        { id -> state.models.firstOrNull { it.manifest.sha256 == id }?.let(model::delete) }, model::import,
                        model::clearError, { t -> model.settings(state.settings.copy(threads = t.threads, context = t.context,
                            maxTokens = t.maxTokens, temperature = t.temperature, topP = t.topP, topK = t.topK,
                            thinking = t.thinking, memoryEfficient = t.memoryEfficient)) }, model::outputLimit, null),
                    options, { ModelManifest.parse(it).name })
            }
        }
    }
    override fun onStart() { super.onStart(); if (::model.isInitialized) model.setForeground(true) }
    override fun onResume() { super.onResume(); if (::model.isInitialized) model.setForeground(true) }
    override fun onStop() { if (::model.isInitialized) model.setForeground(false); super.onStop() }
    override fun onTrimMemory(level: Int) { super.onTrimMemory(level); if (::model.isInitialized) model.trimMemory(level) }
}
