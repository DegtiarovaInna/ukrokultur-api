package de.ukrokultur.ukrokultur_api.news;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "news")
public class News {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "public_id", nullable = false, unique = true)
    private UUID publicId;
    @Column(name = "slug", unique = true)
    private String slug;
    @Column(name="published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "published", nullable = false)
    private boolean published = true;
    @Column(name = "event_date")
    private java.time.LocalDate eventDate;
    @Column(name="video_url")
    private String videoUrl;
    @Column(name = "video_type")
    private String videoType;

    @Column(name = "video_label")
    private String videoLabel;

    @OneToMany(mappedBy = "news", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NewsTranslation> translations = new ArrayList<>();

    @OneToMany(mappedBy = "news", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<NewsImage> images = new ArrayList<>();

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
    public OffsetDateTime getPublishedAt() { return publishedAt; }
    public String getVideoUrl() { return videoUrl; }
    public List<NewsTranslation> getTranslations() { return translations; }
    public List<NewsImage> getImages() { return images; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public String getSlug() { return slug; }
    public java.time.LocalDate getEventDate() { return eventDate; }
    public boolean isPublished() { return published; }
    public String getVideoType() { return videoType; }
    public String getVideoLabel() { return videoLabel; }

    public void setSlug(String slug) { this.slug = slug; }
    public void setEventDate(java.time.LocalDate eventDate) { this.eventDate = eventDate; }
    public void setPublished(boolean published) { this.published = published; }
    public void setVideoType(String videoType) { this.videoType = videoType; }
    public void setVideoLabel(String videoLabel) { this.videoLabel = videoLabel; }
    public void setPublishedAt(OffsetDateTime publishedAt) { this.publishedAt = publishedAt; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }


    public void addTranslation(NewsTranslation t) {
        t.setNews(this);
        this.translations.add(t);
    }

    public void clearTranslations() {
        this.translations.clear();
    }

    public void addImage(NewsImage img) {
        img.setNews(this);
        this.images.add(img);
    }

    public void clearImages() {
        this.images.clear();
    }
}
