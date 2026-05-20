package com.example.leadershipcompass_capstoneprojectbackend.config;

import com.example.leadershipcompass_capstoneprojectbackend.model.Modules;
import com.example.leadershipcompass_capstoneprojectbackend.repository.ModulesRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

/**
 * Startup loader that imports module definitions from JSON resources.
 *
 * <p>When enabled by configuration, this runner reads module JSON files,
 * maps them into {@link Modules} entities, and upserts records into the
 * database using book + title as the idempotent matching key.</p>
 */
@Component
public class ModulesSeedLoader implements CommandLineRunner {

    private final ModulesRepository modulesRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${app.modules.seed.enabled:false}")
    private boolean seedEnabled;

    @Value("${app.modules.seed.path:classpath*:seed/modules/*.json}")
    private String seedPath;

    public ModulesSeedLoader(ModulesRepository modulesRepository) {
        this.modulesRepository = modulesRepository;
    }

    /**
     * Executes module seeding at startup when enabled.
     *
     * @param args command-line args passed to the Spring Boot app
     * @throws Exception when resource loading or parsing fails
     */
    @Override
    public void run(String... args) throws Exception {
        if (!seedEnabled) {
            return;
        }

        Resource[] resources = new PathMatchingResourcePatternResolver().getResources(seedPath);
        if (resources.length == 0) {
            System.out.println("Modules seed is enabled but no JSON files found at: " + seedPath);
            return;
        }

        int upserted = 0;
        for (Resource resource : resources) {
            upserted += importFromResource(resource);
        }

        System.out.println("Modules seed complete. Upserted modules: " + upserted);
    }

    /**
     * Imports one JSON resource and upserts all contained module entries.
     *
     * @param resource JSON file resource
     * @return number of module entries processed from the resource
     * @throws IOException when JSON cannot be read
     */
    private int importFromResource(Resource resource) throws IOException {
        JsonNode root = objectMapper.readTree(resource.getInputStream());
        if (root == null || root.isNull()) {
            return 0;
        }

        int count = 0;
        if (root.isArray()) {
            for (JsonNode moduleNode : root) {
                upsertModule(moduleNode);
                count++;
            }
            return count;
        }

        if (root.has("modules") && root.get("modules").isArray()) {
            for (JsonNode moduleNode : root.get("modules")) {
                upsertModule(moduleNode);
                count++;
            }
            return count;
        }

        upsertModule(root);
        return 1;
    }

    /**
     * Maps a JSON object to a module entity and persists it.
     *
     * @param moduleNode JSON object representing one module
     */
    private void upsertModule(JsonNode moduleNode) {
        String book = getText(moduleNode, "book");
        String title = getText(moduleNode, "title");
        if (book.isBlank() || title.isBlank()) {
            throw new IllegalArgumentException("Each module JSON item must include non-empty 'book' and 'title'.");
        }

        Modules module = modulesRepository
                .findByBookAndTitle(book, title)
                .orElseGet(Modules::new);

        module.setBook(book);
        module.setTitle(title);
        module.setCategory(getText(moduleNode, "category"));
        module.setDescription(getText(moduleNode, "description"));
        module.setDisplayOrder(getInt(moduleNode, "displayOrder", 0));
        module.setActive(getBoolean(moduleNode, "active", true));
        module.setSourceChapters(getStringList(moduleNode, "sourceChapters"));
        module.setChecklist(getStringList(moduleNode, "checklist"));
        module.setQuotesAndConcepts(getStringList(moduleNode, "quotesAndConcepts"));
        module.setActivities(getStringList(moduleNode, "activities"));

        modulesRepository.save(module);
    }

    /**
     * Reads a text value from a JSON object field.
     *
     * @param node source JSON object
     * @param fieldName field to read
     * @return text value, or empty string when missing/null
     */
    private String getText(JsonNode node, String fieldName) {
        JsonNode child = node.get(fieldName);
        return child == null || child.isNull() ? "" : child.asText("");
    }

    /**
     * Reads an integer value from a JSON object field.
     *
     * @param node source JSON object
     * @param fieldName field to read
     * @param defaultValue fallback value when field is missing/null
     * @return parsed integer or fallback value
     */
    private int getInt(JsonNode node, String fieldName, int defaultValue) {
        JsonNode child = node.get(fieldName);
        return child == null || child.isNull() ? defaultValue : child.asInt(defaultValue);
    }

    /**
     * Reads a boolean value from a JSON object field.
     *
     * @param node source JSON object
     * @param fieldName field to read
     * @param defaultValue fallback value when field is missing/null
     * @return parsed boolean or fallback value
     */
    private boolean getBoolean(JsonNode node, String fieldName, boolean defaultValue) {
        JsonNode child = node.get(fieldName);
        return child == null || child.isNull() ? defaultValue : child.asBoolean(defaultValue);
    }

    /**
     * Reads a list of strings from a JSON field.
     *
     * <p>If the field is a scalar, it is converted into a single-item list.</p>
     *
     * @param node source JSON object
     * @param fieldName field to read
     * @return list of string values, or empty list when missing/null
     */
    private List<String> getStringList(JsonNode node, String fieldName) {
        JsonNode child = node.get(fieldName);
        if (child == null || child.isNull()) {
            return Collections.emptyList();
        }

        if (!child.isArray()) {
            List<String> single = new ArrayList<>();
            single.add(child.asText(""));
            return single;
        }

        List<String> values = new ArrayList<>();
        child.forEach(item -> values.add(item.asText("")));
        return values;
    }
}
