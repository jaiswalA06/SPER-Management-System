package prototype;

import enums.IssueSeverity;
import enums.IssueType;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Prototype Pattern — Prototype Registry: keeps the ready-made blueprints and hands out clones of them.
// Callers never receive the stored originals, so the registered blueprints cannot be changed by accident.
public final class IssueTemplateRegistry {

    private final Map<String, IssueTemplate> templates = new LinkedHashMap<>();

    public IssueTemplateRegistry() {
        loadDefaults();
    }

    // Stores its own copy, so later changes to the object passed in do not alter the registry
    public void register(String key, IssueTemplate template) {
        templates.put(normalize(key), template.clone());
    }

    // Returns a fresh clone of the blueprint, or empty if the key is unknown
    public Optional<IssueTemplate> get(String key) {
        IssueTemplate template = templates.get(normalize(key));
        return template == null ? Optional.empty() : Optional.of(template.clone());
    }

    public List<String> keys() {
        return new ArrayList<>(templates.keySet());
    }

    public int size() {
        return templates.size();
    }

    // One default blueprint per issue type. Severities follow the same rules Main uses for new reports.
    private void loadDefaults() {
        register(IssueType.LOST.name(), new IssueTemplate(
                "Lost Package", IssueType.LOST,
                "Package has not reached the destination and tracking shows no movement",
                IssueSeverity.HIGH, 1000.0));

        register(IssueType.DAMAGED.name(), new IssueTemplate(
                "Damaged Package", IssueType.DAMAGED,
                "Package arrived with visible damage to the packaging or contents",
                IssueSeverity.MEDIUM, 1000.0));

        register(IssueType.DELAYED.name(), new IssueTemplate(
                "Delayed Package", IssueType.DELAYED,
                "Package was not delivered by the promised delivery date",
                IssueSeverity.LOW, 1000.0));

        register(IssueType.WRONG_ADDRESS.name(), new IssueTemplate(
                "Wrong Address", IssueType.WRONG_ADDRESS,
                "Package was routed or delivered to an incorrect address",
                IssueSeverity.MEDIUM, 1000.0));

        register(IssueType.WRONG_PACKAGE.name(), new IssueTemplate(
                "Wrong Package", IssueType.WRONG_PACKAGE,
                "Customer received an item different from the one ordered",
                IssueSeverity.LOW, 1000.0));

        register(IssueType.RECIPIENT_UNAVAILABLE.name(), new IssueTemplate(
                "Recipient Unavailable", IssueType.RECIPIENT_UNAVAILABLE,
                "Delivery was attempted but the recipient was not available",
                IssueSeverity.LOW, 1000.0));
    }

    private static String normalize(String key) {
        return key == null ? "" : key.trim().toUpperCase();
    }
}
