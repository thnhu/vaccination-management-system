package Vaccination.Management.System.advisor.llm;

public record LlmToolResultBlock(String toolUseId, String content) implements LlmContentBlock {
}
