package Vaccination.Management.System.advisor.llm;

public sealed interface LlmContentBlock
        permits LlmTextBlock, LlmToolUseBlock, LlmToolResultBlock {
}
