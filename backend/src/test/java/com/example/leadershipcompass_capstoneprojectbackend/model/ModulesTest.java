package com.example.leadershipcompass_capstoneprojectbackend.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test class for testing the Modules model.
 *
 * <p>This class verifies that the getter and setter methods
 * in the Modules entity work correctly, and that default
 * values are initialised as expected.</p>
 *
 * <p>These tests focus on object behaviour only and do not
 * interact with the database.</p>
 */
class ModulesTest {

    /**
     * Tests whether the basic module fields can be set
     * and retrieved correctly.
     *
     * <p>This includes the module ID, title, category,
     * book, description, display order, and active status.</p>
     */
    @Test
    void shouldSetAndGetModuleBasicFields() {

        // Creates a new module object
        Modules module = new Modules();

        // Sets the module fields
        module.setId(1L);
        module.setTitle("Caring Time Basics");
        module.setCategory("Caring Time");
        module.setBook("5 Leadership Languages");
        module.setDescription("Build trust through focused time.");
        module.setDisplayOrder(1);
        module.setActive(true);

        // Verifies the values were stored correctly
        assertEquals(1L, module.getId());
        assertEquals("Caring Time Basics", module.getTitle());
        assertEquals("Caring Time", module.getCategory());
        assertEquals("5 Leadership Languages", module.getBook());
        assertEquals("Build trust through focused time.", module.getDescription());
        assertEquals(1, module.getDisplayOrder());
        assertTrue(module.getActive());
    }

    /**
     * Tests whether source chapter values can be stored
     * and retrieved correctly.
     */
    @Test
    void shouldSetAndGetSourceChapters() {

        // Creates a new module object
        Modules module = new Modules();

        // Adds source chapters
        module.setSourceChapters(List.of("Chapter 1", "Chapter 2"));

        // Verifies the chapter list contents
        assertEquals(2, module.getSourceChapters().size());
        assertEquals("Chapter 1", module.getSourceChapters().get(0));
        assertEquals("Chapter 2", module.getSourceChapters().get(1));
    }

    /**
     * Tests whether checklist items can be stored
     * and retrieved correctly.
     */
    @Test
    void shouldSetAndGetChecklistItems() {

        // Creates a new module object
        Modules module = new Modules();

        // Adds checklist items
        module.setChecklist(List.of(
                "Schedule regular one-on-one meetings",
                "Protect time from interruptions"
        ));

        // Verifies the checklist contents
        assertEquals(2, module.getChecklist().size());
        assertEquals("Schedule regular one-on-one meetings", module.getChecklist().get(0));
        assertEquals("Protect time from interruptions", module.getChecklist().get(1));
    }

    /**
     * Tests whether quotes and concepts can be stored
     * and retrieved correctly.
     */
    @Test
    void shouldSetAndGetQuotesAndConcepts() {

        // Creates a new module object
        Modules module = new Modules();

        // Adds quote/concept values
        module.setQuotesAndConcepts(List.of(
                "Listen to understand, not just respond"
        ));

        // Verifies the stored values
        assertEquals(1, module.getQuotesAndConcepts().size());
        assertEquals("Listen to understand, not just respond",
                module.getQuotesAndConcepts().get(0));
    }

    /**
     * Tests whether activity items can be stored
     * and retrieved correctly.
     */
    @Test
    void shouldSetAndGetActivities() {

        // Creates a new module object
        Modules module = new Modules();

        // Adds activity items
        module.setActivities(List.of(
                "Complete a weekly reflection activity"
        ));

        // Verifies the stored activities
        assertEquals(1, module.getActivities().size());
        assertEquals("Complete a weekly reflection activity",
                module.getActivities().get(0));
    }

    /**
     * Tests whether the active field defaults to true
     * when a new module object is created.
     */
    @Test
    void activeShouldDefaultToTrue() {

        // Creates a new module object
        Modules module = new Modules();

        // Verifies the default active value
        assertTrue(module.getActive());
    }

    /**
     * Tests whether list-based fields are automatically
     * initialised as empty lists.
     *
     * <p>This prevents null pointer issues when accessing
     * module content collections.</p>
     */
    @Test
    void listFieldsShouldDefaultToEmptyLists() {

        // Creates a new module object
        Modules module = new Modules();

        // Verifies the lists were initialised
        assertNotNull(module.getSourceChapters());
        assertNotNull(module.getChecklist());
        assertNotNull(module.getQuotesAndConcepts());
        assertNotNull(module.getActivities());

        // Verifies the lists start empty
        assertTrue(module.getSourceChapters().isEmpty());
        assertTrue(module.getChecklist().isEmpty());
        assertTrue(module.getQuotesAndConcepts().isEmpty());
        assertTrue(module.getActivities().isEmpty());
    }
}