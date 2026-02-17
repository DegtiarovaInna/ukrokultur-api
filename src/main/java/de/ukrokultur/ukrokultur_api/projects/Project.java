package de.ukrokultur.ukrokultur_api.projects;


import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "project")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;
    @Column(name="slug", unique = true, length = 200)
    private String slug;

    @Column(name="published", nullable = false)
    private boolean published = true;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectTranslation> translations = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<ProjectImage> images = new ArrayList<>();

    @Column(name="created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name="updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();
    @PrePersist
    public void prePersist() {
        if (publicId == null) publicId = UUID.randomUUID();
    }
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public UUID getPublicId() { return publicId; }
    public void setPublicId(UUID publicId) { this.publicId = publicId; }
    public String getSlug() { return slug; }
    public boolean isPublished() { return published; }
    public List<ProjectTranslation> getTranslations() { return translations; }
    public List<ProjectImage> getImages() { return images; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void setSlug(String slug) { this.slug = slug; }
    public void setPublished(boolean published) { this.published = published; }

    public void addTranslation(ProjectTranslation t) {
        t.setProject(this);
        this.translations.add(t);
    }

    public void clearTranslations() {
        this.translations.clear();
    }

    public void addImage(ProjectImage img) {
        img.setProject(this);
        this.images.add(img);
    }

    public void clearImages() {
        this.images.clear();
    }
}
