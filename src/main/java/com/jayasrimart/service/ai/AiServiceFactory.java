package com.jayasrimart.service.ai;

/**
 * Factory providing the appropriate {@link AiServiceProvider} instance.
 */
public final class AiServiceFactory {

    private static final GeminiAiServiceProvider GEMINI_PROVIDER = new GeminiAiServiceProvider();
    private static final MockAiServiceProvider MOCK_PROVIDER = new MockAiServiceProvider();

    private AiServiceFactory() {
        // Prevent instantiation
    }

    /**
     * Resolves the AI service provider, favoring Gemini API when configured and falling back to Mock.
     *
     * @return active AI service provider
     */
    public static AiServiceProvider getProvider() {
        if (GEMINI_PROVIDER.isAvailable()) {
            return GEMINI_PROVIDER;
        }
        return MOCK_PROVIDER;
    }
}
