package com.example.leadershipcompass_capstoneprojectbackend.model;

import jakarta.persistence.Column;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA entity representing a single learning module.
 *
 * <p>Each module stores core metadata (category, title, description, and display order)
 * plus ordered supporting content lists used by the learning experience.</p>
 */
@Entity
@Table(name = "modules")
public class Modules {

    /** Auto-incremented primary key for the module. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** High-level module category (e.g., Caring Time, Psychological Touch). */
    @Column(nullable = false, length = 80)
    private String category;

    /** Title of the source book this module belongs to. */
    @Column(nullable = false, length = 200)
    private String book;

    /** Human-readable module title shown in the UI. */
    @Column(nullable = false, length = 200)
    private String title;

    /** Detailed explanation of the module topic and learning objective. */
    @Column(length = 2000)
    private String description;

    /** Numeric ordering used to control module sequence in views. */
    @Column(nullable = false)
    private Integer displayOrder;

    /** Whether the module is currently enabled and available to users. */
    @Column(nullable = false)
    private Boolean active = true;

    /** Ordered list of source chapters used to build this module. */
    @ElementCollection
    @CollectionTable(name = "module_source_chapters", joinColumns = @JoinColumn(name = "module_id"))
    @Column(name = "source_chapter", length = 200)
    @OrderColumn(name = "chapter_order")
    private List<String> sourceChapters = new ArrayList<>();

    /** Ordered checklist items for practicing module behaviors. */
    @ElementCollection
    @CollectionTable(name = "module_checklist_items", joinColumns = @JoinColumn(name = "module_id"))
    @Column(name = "checklist_item", length = 1000)
    @OrderColumn(name = "item_order")
    private List<String> checklist = new ArrayList<>();

    /** Ordered list of quotes and key concepts associated with the module. */
    @ElementCollection
    @CollectionTable(name = "module_quotes_and_concepts", joinColumns = @JoinColumn(name = "module_id"))
    @Column(name = "quote_or_concept", length = 1000)
    @OrderColumn(name = "quote_order")
    private List<String> quotesAndConcepts = new ArrayList<>();

    /** Ordered list of practical activities to apply the module content. */
    @ElementCollection
    @CollectionTable(name = "module_activities", joinColumns = @JoinColumn(name = "module_id"))
    @Column(name = "activity", length = 1000)
    @OrderColumn(name = "activity_order")
    private List<String> activities = new ArrayList<>();

    /**
     * Returns the database identifier for this module.
     *
     * @return auto-generated module id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the database identifier for this module.
     *
     * @param id module id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the module title.
     *
     * @return module title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the module title.
     *
     * @param title module title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the module category.
     *
     * @return module category
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the module category.
     *
     * @param category module category
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Returns the source book title for this module.
     *
     * @return source book title
     */
    public String getBook() {
        return book;
    }

    /**
     * Sets the source book title for this module.
     *
     * @param book source book title
     */
    public void setBook(String book) {
        this.book = book;
    }

    /**
     * Returns the module description.
     *
     * @return module description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the module description.
     *
     * @param description module description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the module display order.
     *
     * @return ordering index used for sorting modules
     */
    public Integer getDisplayOrder() {
        return displayOrder;
    }

    /**
     * Sets the module display order.
     *
     * @param displayOrder ordering index used for sorting modules
     */
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    /**
     * Indicates whether the module is active.
     *
     * @return true when active, otherwise false
     */
    public Boolean getActive() {
        return active;
    }

    /**
     * Sets whether the module is active.
     *
     * @param active true to enable module, false to disable
     */
    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * Returns the ordered source chapters for this module.
     *
     * @return source chapter list
     */
    public List<String> getSourceChapters() {
        return sourceChapters;
    }

    /**
     * Sets the ordered source chapters for this module.
     *
     * @param sourceChapters source chapter list
     */
    public void setSourceChapters(List<String> sourceChapters) {
        this.sourceChapters = sourceChapters;
    }

    /**
     * Returns the ordered checklist items for this module.
     *
     * @return checklist item list
     */
    public List<String> getChecklist() {
        return checklist;
    }

    /**
     * Sets the ordered checklist items for this module.
     *
     * @param checklist checklist item list
     */
    public void setChecklist(List<String> checklist) {
        this.checklist = checklist;
    }

    /**
     * Returns the ordered quotes and concepts for this module.
     *
     * @return quote and concept list
     */
    public List<String> getQuotesAndConcepts() {
        return quotesAndConcepts;
    }

    /**
     * Sets the ordered quotes and concepts for this module.
     *
     * @param quotesAndConcepts quote and concept list
     */
    public void setQuotesAndConcepts(List<String> quotesAndConcepts) {
        this.quotesAndConcepts = quotesAndConcepts;
    }

    /**
     * Returns the ordered activities for this module.
     *
     * @return activity list
     */
    public List<String> getActivities() {
        return activities;
    }

    /**
     * Sets the ordered activities for this module.
     *
     * @param activities activity list
     */
    public void setActivities(List<String> activities) {
        this.activities = activities;
    }
}
