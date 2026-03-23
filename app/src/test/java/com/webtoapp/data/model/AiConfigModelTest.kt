package com.webtoapp.data.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AiConfigModelTest {

    @Test
    fun `api key config prefers custom endpoints when provided`() {
        val apiKey = ApiKeyConfig(
            provider = AiProvider.OPENAI,
            apiKey = "k",
            customModelsEndpoint = "/custom/models",
            customChatEndpoint = "/custom/chat"
        )

        assertThat(apiKey.getEffectiveModelsEndpoint()).isEqualTo("/custom/models")
        assertThat(apiKey.getEffectiveChatEndpoint()).isEqualTo("/custom/chat")
    }

    @Test
    fun `api key config falls back to provider defaults`() {
        val openAi = ApiKeyConfig(provider = AiProvider.OPENAI, apiKey = "k")
        val glm = ApiKeyConfig(provider = AiProvider.GLM, apiKey = "k")
        val volcano = ApiKeyConfig(provider = AiProvider.VOLCANO, apiKey = "k")
        val anthropic = ApiKeyConfig(provider = AiProvider.ANTHROPIC, apiKey = "k")
        val google = ApiKeyConfig(provider = AiProvider.GOOGLE, apiKey = "k")

        assertThat(openAi.getEffectiveModelsEndpoint()).isEqualTo("/v1/models")
        assertThat(openAi.getEffectiveChatEndpoint()).isEqualTo("/v1/chat/completions")
        assertThat(glm.getEffectiveChatEndpoint()).isEqualTo("/v4/chat/completions")
        assertThat(volcano.getEffectiveChatEndpoint()).isEqualTo("/v3/chat/completions")
        assertThat(anthropic.getEffectiveChatEndpoint()).isEqualTo("/v1/messages")
        assertThat(google.getEffectiveChatEndpoint()).isEqualTo("/v1beta/models")
    }

    @Test
    fun `saved model infers supported features from capabilities`() {
        val model = SavedModel(
            model = AiModel(
                id = "m1",
                name = "demo",
                provider = AiProvider.OPENAI,
                capabilities = emptyList()
            ),
            apiKeyId = "key-1",
            capabilities = listOf(ModelCapability.TEXT, ModelCapability.IMAGE_GENERATION)
        )

        val features = model.getSupportedFeatures()

        assertThat(features).contains(AiFeature.GENERAL)
        assertThat(features).contains(AiFeature.TRANSLATION)
        assertThat(features).contains(AiFeature.ICON_GENERATION)
        assertThat(features).contains(AiFeature.HTML_CODING_IMAGE)
    }

    @Test
    fun `saved model custom feature mapping overrides defaults`() {
        val model = SavedModel(
            model = AiModel(
                id = "m2",
                name = "demo",
                provider = AiProvider.OPENAI,
                capabilities = emptyList()
            ),
            apiKeyId = "key-2",
            capabilities = listOf(ModelCapability.TEXT),
            featureMappings = mapOf(
                ModelCapability.TEXT to setOf(AiFeature.GENERAL)
            )
        )

        val features = model.getSupportedFeatures()

        assertThat(features).containsExactly(AiFeature.GENERAL)
        assertThat(model.supportsFeature(AiFeature.GENERAL)).isTrue()
        assertThat(model.supportsFeature(AiFeature.TRANSLATION)).isFalse()
        assertThat(model.getFeaturesForCapability(ModelCapability.TEXT)).containsExactly(AiFeature.GENERAL)
    }
}

