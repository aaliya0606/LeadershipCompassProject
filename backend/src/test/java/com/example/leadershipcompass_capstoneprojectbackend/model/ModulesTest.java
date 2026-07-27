package com.example.leadershipcompass_capstoneprojectbackend.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Modules} entity.
 *
 * <p>These tests verify that the entity correctly stores and retrieves
 * module information, including metadata, learning content, and
 * collection-based fields through its getter and setter methods.</p>
 */

class ModulesTest {

    /**
     * Verifies that the module ID can be retrieved correctly.
     */
    @Test
    void getId() {
        Modules module = new Modules();
        module.setId(1L);

        assertEquals(1L, module.getId());
    }
    /**
     * Verifies that the module ID is stored correctly.
     */
    @Test
    void setId() {
        Modules module = new Modules();

        module.setId(2L);

        assertEquals(2L, module.getId());
    }
    /**
     * Verifies that the module title can be retrieved correctly.
     */
    @Test
    void getTitle() {
        Modules module = new Modules();
        module.setTitle("Leading Under Pressure");

        assertEquals("Leading Under Pressure", module.getTitle());
    }
    /**
     * Verifies that the module title is stored correctly.
     */
    @Test
    void setTitle() {
        Modules module = new Modules();

        module.setTitle("Conscious Control");

        assertEquals("Conscious Control", module.getTitle());
    }
    /**
     * Verifies that the module category can be retrieved correctly.
     */
    @Test
    void getCategory() {
        Modules module = new Modules();
        module.setCategory("Caring Time");

        assertEquals("Caring Time", module.getCategory());
    }
    /**
     * Verifies that the module category is stored correctly.
     */
    @Test
    void setCategory() {
        Modules module = new Modules();

        module.setCategory("Psychological Touch");

        assertEquals("Psychological Touch", module.getCategory());
    }
    /**
     * Verifies that the source book can be retrieved correctly.
     */
    @Test
    void getBook() {
        Modules module = new Modules();
        module.setBook("The Leadership Compass");

        assertEquals("The Leadership Compass", module.getBook());
    }
    /**
     * Verifies that the source book is stored correctly.
     */
    @Test
    void setBook() {
        Modules module = new Modules();

        module.setBook("Leading Under Pressure");

        assertEquals("Leading Under Pressure", module.getBook());
    }
    /**
     * Verifies that the module description can be retrieved correctly.
     */
    @Test
    void getDescription() {
        Modules module = new Modules();
        module.setDescription("A module focused on calm and controlled leadership.");

        assertEquals(
                "A module focused on calm and controlled leadership.",
                module.getDescription()
        );
    }
    /**
     * Verifies that the module description is stored correctly.
     */
    @Test
    void setDescription() {
        Modules module = new Modules();

        module.setDescription("Develops practical leadership behaviours.");

        assertEquals(
                "Develops practical leadership behaviours.",
                module.getDescription()
        );
    }
    /**
     * Verifies that the module display order can be retrieved correctly.
     */
    @Test
    void getDisplayOrder() {
        Modules module = new Modules();
        module.setDisplayOrder(1);

        assertEquals(1, module.getDisplayOrder());
    }
    /**
     * Verifies that the module display order is stored correctly.
     */
    @Test
    void setDisplayOrder() {
        Modules module = new Modules();

        module.setDisplayOrder(3);

        assertEquals(3, module.getDisplayOrder());
    }
    /**
     * Verifies that new modules are active by default.
     */
    @Test
    void getActive() {
        Modules module = new Modules();

        assertTrue(module.getActive());
    }
    /**
     * Verifies that the active status of a module can be updated.
     */
    @Test
    void setActive() {
        Modules module = new Modules();

        module.setActive(false);

        assertFalse(module.getActive());
    }
    /**
     * Verifies that the ordered list of source chapters can be retrieved correctly.
     */
    @Test
    void getSourceChapters() {
        Modules module = new Modules();
        List<String> chapters = Arrays.asList("Chapter 1", "Chapter 2");

        module.setSourceChapters(chapters);

        assertEquals(chapters, module.getSourceChapters());
        assertEquals(2, module.getSourceChapters().size());
    }
    /**
     * Verifies that the ordered list of source chapters is stored correctly.
     */
    @Test
    void setSourceChapters() {
        Modules module = new Modules();
        List<String> chapters = Arrays.asList(
                "Conscious Control",
                "Care Factor",
                "Courage"
        );

        module.setSourceChapters(chapters);

        assertEquals(chapters, module.getSourceChapters());
    }
    /**
     * Verifies that the checklist items can be retrieved correctly.
     */
    @Test
    void getChecklist() {
        Modules module = new Modules();
        List<String> checklist = Arrays.asList(
                "Reflect on leadership behaviour",
                "Practise active listening"
        );

        module.setChecklist(checklist);

        assertEquals(checklist, module.getChecklist());
        assertEquals(2, module.getChecklist().size());
    }
    /**
     * Verifies that the checklist items are stored correctly.
     */
    @Test
    void setChecklist() {
        Modules module = new Modules();
        List<String> checklist = Arrays.asList(
                "Complete reflection activity",
                "Record weekly progress"
        );

        module.setChecklist(checklist);

        assertEquals(checklist, module.getChecklist());
    }
    /**
     * Verifies that the quotes and concepts can be retrieved correctly.
     */
    @Test
    void getQuotesAndConcepts() {
        Modules module = new Modules();
        List<String> quotesAndConcepts = Arrays.asList(
                "Lead with conscious control",
                "Remain calm under pressure"
        );

        module.setQuotesAndConcepts(quotesAndConcepts);

        assertEquals(quotesAndConcepts, module.getQuotesAndConcepts());
        assertEquals(2, module.getQuotesAndConcepts().size());
    }
    /**
     * Verifies that the quotes and concepts are stored correctly.
     */
    @Test
    void setQuotesAndConcepts() {
        Modules module = new Modules();
        List<String> quotesAndConcepts = Arrays.asList(
                "Care Factor",
                "Psychological Safety"
        );

        module.setQuotesAndConcepts(quotesAndConcepts);

        assertEquals(quotesAndConcepts, module.getQuotesAndConcepts());
    }
    /**
     * Verifies that the learning activities can be retrieved correctly.
     */
    @Test
    void getActivities() {
        Modules module = new Modules();
        List<String> activities = Arrays.asList(
                "Complete a weekly reflection",
                "Practise a leadership conversation"
        );

        module.setActivities(activities);

        assertEquals(activities, module.getActivities());
        assertEquals(2, module.getActivities().size());
    }
    /**
     * Verifies that the learning activities are stored correctly.
     */
    @Test
    void setActivities() {
        Modules module = new Modules();
        List<String> activities = Arrays.asList(
                "Leadership journal",
                "Team feedback activity"
        );

        module.setActivities(activities);

        assertEquals(activities, module.getActivities());
    }
}