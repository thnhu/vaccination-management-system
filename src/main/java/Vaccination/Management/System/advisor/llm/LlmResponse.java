package Vaccination.Management.System.advisor.llm;

import java.util.List;

public class LlmResponse {

    private final List<LlmContentBlock> content;
    private final LlmStopReason stopReason;

    public LlmResponse(List<LlmContentBlock> content, LlmStopReason stopReason) {
        this.content = content;
        this.stopReason = stopReason;
    }

    public List<LlmContentBlock> getContent() { return content; }
    public LlmStopReason getStopReason() { return stopReason; }
}
