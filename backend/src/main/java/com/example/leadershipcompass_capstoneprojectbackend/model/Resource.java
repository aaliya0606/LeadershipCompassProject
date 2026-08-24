package com.example.leadershipcompass_capstoneprojectbackend.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents an educational resource available through the Leadership Compass
 * Resource Library.
 *
 * <p>A Resource stores the metadata required to classify, display, and retrieve
 * learning materials such as PDFs, videos, audio files, eBooks, documents,
 * images, and external resources.</p>
 *
 * <p>Resources may reference either a locally stored file using
 * {@code storageProvider} and {@code storageKey}, or an external resource using
 * {@code resourceUrl}. File contents themselves are not stored in the database.</p>
 *
 * <p>The {@code leadershipLanguage} field associates the resource with one of
 * the Leadership Compass leadership-language categories. Resources that have
 * not yet been classified may use {@code UNCLASSIFIED}.</p>
 */

@Entity
@Table(name = "resources")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique database identifier generated automatically when the resource
     * metadata is persisted.
     */

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private String leadershipLanguage;

    @Column(nullable = false)
    private String resourceType;

    /**
     * URL for resources hosted externally rather than in local file storage.
     * This value is normally null for uploaded files.
     */

    private String resourceUrl;

    /**
     * Determines whether the resource is available to the user-facing
     * Resource Library.
     */

    private Boolean active;

    /**
     * Controls the order in which active resources are presented.
     * Lower values are returned before higher values.
     */

    private Integer displayOrder;

    /**
     * Original name of an uploaded file. Used for display and when returning
     * the file to the client.
     */
    
    private String originalFileName;

    /**
     * MIME type of the stored file, for example {@code application/pdf}.
     * Used when serving the file through the Resource API.
     */

    private String contentType;

    /**
     * Size of the uploaded file in bytes.
     */
    private Long fileSize;

    /**
     * Identifies the storage mechanism containing the physical file.
     * Currently {@code LOCAL} is used for files stored under resource-storage.
     */
    private String storageProvider;

    /**
     * Path of the stored file relative to the configured Resource Library
     * storage directory.
     *
     * <p>This is an internal backend storage reference. Frontend clients should
     * retrieve files through the Resource API rather than constructing paths
     * from this value.</p>
     */
    private String storageKey;
}
