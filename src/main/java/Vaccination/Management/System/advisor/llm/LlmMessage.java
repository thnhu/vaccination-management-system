package Vaccination.Management.System.advisor.llm;

import java.util.ArrayList;
import java.util.List;

public class LlmMessage {

    private final LlmRole role;
    private final List<LlmContentBlock> content;

    private LlmMessage(LlmRole role, List<LlmContentBlock> content) {
        this.role = role;
        this.content = content;
    }

    public LlmRole getRole() { return role; }
    public List<LlmContentBlock> getContent() { return content; }

    public static LlmMessage user(String text) {
        return new LlmMessage(LlmRole.USER, List.of(new LlmTextBlock(text)));
    }

    public static LlmMessage assistant(List<LlmContentBlock> content) {
        return new LlmMessage(LlmRole.ASSISTANT, List.copyOf(content));
    }

    public static LlmMessage toolResults(List<LlmToolResultBlock> results) {
        return new LlmMessage(LlmRole.USER, new ArrayList<>(results));
    }
}
