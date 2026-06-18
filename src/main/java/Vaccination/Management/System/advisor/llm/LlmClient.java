package Vaccination.Management.System.advisor.llm;

import java.util.List;

public interface LlmClient {
    LlmResponse chat(String systemPrompt, List<LlmToolDefinition> tools, List<LlmMessage> history);
}
