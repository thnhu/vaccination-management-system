package Vaccination.Management.System.advisor.config;

import Vaccination.Management.System.advisor.llm.LlmClient;
import Vaccination.Management.System.advisor.llm.impl.AnthropicLlmClientImpl;
import com.anthropic.client.AnthropicClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LlmConfig {

    @Bean
    @ConditionalOnProperty(name = "advisor.llm.provider", havingValue = "anthropic", matchIfMissing = true)
    public LlmClient anthropicLlmClient(AnthropicClient anthropicClient) {
        return new AnthropicLlmClientImpl(anthropicClient, new ObjectMapper());
    }
}
