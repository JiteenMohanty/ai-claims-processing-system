package com.jiteen.claims.ai.config;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that provisions the Anthropic Claude client used by the
 * real AI analysis backend.
 *
 * <p>
 * The {@link AnthropicClient} bean is only created when {@code ai.provider=claude}
 * is set in the active configuration. When the provider is {@code simulated}
 * (the default), no client is constructed and no Anthropic credentials are
 * required — the service runs entirely on the rule-based simulation.
 * </p>
 *
 * <p>
 * Credentials are resolved from the environment via the SDK's standard
 * resolution chain (primarily the {@code ANTHROPIC_API_KEY} environment
 * variable). No key is ever embedded in source or configuration files.
 * </p>
 *
 * @author Jiteen
 * @version 1.0
 * @since Java 21
 */
@Configuration
public class ClaudeConfig {

    /**
     * Builds the singleton {@link AnthropicClient} from the configured API key.
     *
     * @param apiKey the Anthropic API key, resolved from the {@code ANTHROPIC_API_KEY}
     *               environment variable
     * @return a fully configured Anthropic Claude client
     * @throws IllegalStateException if {@code ai.provider=claude} but no API key is set,
     *                               so the misconfiguration surfaces clearly at startup
     */
    @Bean
    @ConditionalOnProperty(name = "ai.provider", havingValue = "claude")
    public AnthropicClient anthropicClient(@Value("${ANTHROPIC_API_KEY:}") String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "AI_PROVIDER=claude requires ANTHROPIC_API_KEY to be set. "
                    + "Add your Anthropic API key (e.g. in the project .env) and restart, "
                    + "or set AI_PROVIDER=simulated to use the built-in rule-based analysis.");
        }
        return AnthropicOkHttpClient.builder().apiKey(apiKey).build();
    }
}
