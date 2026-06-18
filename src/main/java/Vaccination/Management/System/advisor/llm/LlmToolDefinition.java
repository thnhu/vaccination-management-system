package Vaccination.Management.System.advisor.llm;

import java.util.List;
import java.util.Map;

public class LlmToolDefinition {

    private final String name;
    private final String description;
    private final Map<String, Object> properties;
    private final List<String> required;

    public LlmToolDefinition(String name, String description,
                              Map<String, Object> properties, List<String> required) {
        this.name = name;
        this.description = description;
        this.properties = properties;
        this.required = required;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Map<String, Object> getProperties() { return properties; }
    public List<String> getRequired() { return required; }
}
