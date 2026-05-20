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

    private String getText(JsonNode node, String fieldName) {
        JsonNode child = node.get(fieldName);
        return child == null || child.isNull() ? "" : child.asText("");
    }

    private int getInt(JsonNode node, String fieldName, int defaultValue) {
        JsonNode child = node.get(fieldName);
        return child == null || child.isNull() ? defaultValue : child.asInt(defaultValue);
    }

    private boolean getBoolean(JsonNode node, String fieldName, boolean defaultValue) {
        JsonNode child = node.get(fieldName);
        return child == null || child.isNull() ? defaultValue : child.asBoolean(defaultValue);
    }

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
