package Vaccination.Management.System.advisor.llm.impl;

import Vaccination.Management.System.advisor.llm.*;
import com.anthropic.client.AnthropicClient;
import com.anthropic.core.JsonValue;
import com.anthropic.models.messages.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class AnthropicLlmClientImpl implements LlmClient {

    private static final String MODEL = "claude-haiku-4-5-20251001";
    private static final long MAX_TOKENS = 1024L;

    private final AnthropicClient anthropicClient;
    private final ObjectMapper objectMapper;

    public AnthropicLlmClientImpl(AnthropicClient anthropicClient, ObjectMapper objectMapper) {
        this.anthropicClient = anthropicClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public LlmResponse chat(String systemPrompt, List<LlmToolDefinition> tools, List<LlmMessage> history) {
        MessageCreateParams.Builder builder = MessageCreateParams.builder()
                .model(MODEL)
                .maxTokens(MAX_TOKENS)
                .system(MessageCreateParams.System.ofTextBlockParams(List.of(
                        TextBlockParam.builder()
                                .text(systemPrompt)
                                .cacheControl(CacheControlEphemeral.builder().build())
                                .build()
                )));

        for (int i = 0; i < tools.size(); i++) {
            builder.addTool(buildTool(tools.get(i), i == tools.size() - 1));
        }
        history.forEach(msg -> builder.addMessage(toMessageParam(msg)));

        Message response = anthropicClient.messages().create(builder.build());
        return toLlmResponse(response);
    }

    // ── Conversion: LlmToolDefinition → Tool ─────────────────────────────────

    private Tool buildTool(LlmToolDefinition def, boolean withCacheControl) {
        Tool.InputSchema.Builder schemaBuilder = Tool.InputSchema.builder();

        if (!def.getRequired().isEmpty()) {
            schemaBuilder.required(def.getRequired());
        }

        if (!def.getProperties().isEmpty()) {
            Tool.InputSchema.Properties.Builder propsBuilder = Tool.InputSchema.Properties.builder();
            def.getProperties().forEach((k, v) ->
                    propsBuilder.putAdditionalProperty(k, JsonValue.from(v)));
            schemaBuilder.properties(propsBuilder.build());
        }

        Tool.Builder toolBuilder = Tool.builder()
                .name(def.getName())
                .description(def.getDescription())
                .inputSchema(schemaBuilder.build());

        if (withCacheControl) {
            toolBuilder.cacheControl(CacheControlEphemeral.builder().build());
        }

        return toolBuilder.build();
    }

    // ── Conversion: LlmMessage → MessageParam ────────────────────────────────

    private MessageParam toMessageParam(LlmMessage msg) {
        if (msg.getRole() == LlmRole.USER) {
            boolean hasToolResults = msg.getContent().stream()
                    .anyMatch(b -> b instanceof LlmToolResultBlock);

            if (hasToolResults) {
                List<ContentBlockParam> blocks = msg.getContent().stream()
                        .filter(b -> b instanceof LlmToolResultBlock)
                        .map(b -> {
                            LlmToolResultBlock tr = (LlmToolResultBlock) b;
                            return ContentBlockParam.ofToolResult(
                                    ToolResultBlockParam.builder()
                                            .toolUseId(tr.toolUseId())
                                            .content(tr.content())
                                            .build());
                        })
                        .toList();
                return MessageParam.builder()
                        .role(MessageParam.Role.USER)
                        .contentOfBlockParams(blocks)
                        .build();
            }

            // Regular text user message
            String text = msg.getContent().stream()
                    .filter(b -> b instanceof LlmTextBlock)
                    .map(b -> ((LlmTextBlock) b).text())
                    .collect(Collectors.joining("\n"));
            return MessageParam.builder()
                    .role(MessageParam.Role.USER)
                    .content(text)
                    .build();
        }

        // ASSISTANT message: may contain text + tool use blocks
        List<ContentBlockParam> blocks = msg.getContent().stream()
                .map(this::toContentBlockParam)
                .filter(Objects::nonNull)
                .toList();

        if (blocks.isEmpty()) {
            return MessageParam.builder()
                    .role(MessageParam.Role.ASSISTANT)
                    .content("")
                    .build();
        }

        return MessageParam.builder()
                .role(MessageParam.Role.ASSISTANT)
                .contentOfBlockParams(blocks)
                .build();
    }

    private ContentBlockParam toContentBlockParam(LlmContentBlock block) {
        return switch (block) {
            case LlmTextBlock tb -> ContentBlockParam.ofText(
                    TextBlockParam.builder().text(tb.text()).build());

            case LlmToolUseBlock tub -> {
                try {
                    Object rawInput = objectMapper.readValue(tub.inputJson(), Object.class);
                    yield ContentBlockParam.ofToolUse(
                            ToolUseBlockParam.builder()
                                    .id(tub.id())
                                    .name(tub.name())
                                    .input(JsonValue.from(rawInput))
                                    .build());
                } catch (Exception e) {
                    yield ContentBlockParam.ofToolUse(
                            ToolUseBlockParam.builder()
                                    .id(tub.id())
                                    .name(tub.name())
                                    .input(JsonValue.from(Map.of()))
                                    .build());
                }
            }

            case LlmToolResultBlock trb -> ContentBlockParam.ofToolResult(
                    ToolResultBlockParam.builder()
                            .toolUseId(trb.toolUseId())
                            .content(trb.content())
                            .build());
        };
    }

    // ── Conversion: Message → LlmResponse ────────────────────────────────────

    private LlmResponse toLlmResponse(Message response) {
        List<LlmContentBlock> content = response.content().stream()
                .map(this::toLlmContentBlock)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        LlmStopReason stopReason = response.stopReason()
                .map(sr -> StopReason.TOOL_USE.equals(sr) ? LlmStopReason.TOOL_USE : LlmStopReason.END_TURN)
                .orElse(LlmStopReason.END_TURN);

        return new LlmResponse(content, stopReason);
    }

    private LlmContentBlock toLlmContentBlock(ContentBlock block) {
        if (block.isText()) {
            return new LlmTextBlock(block.asText().text());
        }
        if (block.isToolUse()) {
            ToolUseBlock toolUse = block.asToolUse();
            try {
                String inputJson = objectMapper.writeValueAsString(toolUse._input());
                return new LlmToolUseBlock(toolUse.id(), toolUse.name(), inputJson);
            } catch (Exception e) {
                return new LlmToolUseBlock(toolUse.id(), toolUse.name(), "{}");
            }
        }
        return null;
    }
}
