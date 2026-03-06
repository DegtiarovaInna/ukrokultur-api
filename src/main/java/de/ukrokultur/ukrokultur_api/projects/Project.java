package de.ukrokultur.ukrokultur_api.projects;

import jakarta.persistence.*;
import java.time.LocalDate;
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

    @Column(name = "slug", unique = true, length = 200)
    private String slug;

    @Column(name="published", nullable = false)
    private boolean published = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "cover_image", columnDefinition = "text")
    private String coverImage;

    @Column(name = "title_en", nullable = false, columnDefinition = "text")
    private String titleEn;
    @Column(name = "title_de", nullable = false, columnDefinition = "text")
    private String titleDe;
    @Column(name = "title_uk", nullable = false, columnDefinition = "text")
    private String titleUk;

    @Column(name = "subtitle_en", columnDefinition = "text")
    private String subtitleEn;
    @Column(name = "subtitle_de", columnDefinition = "text")
    private String subtitleDe;
    @Column(name = "subtitle_uk", columnDefinition = "text")
    private String subtitleUk;

    @Column(name = "description_en", nullable = false, columnDefinition = "text")
    private String descriptionEn;
    @Column(name = "description_de", nullable = false, columnDefinition = "text")
    private String descriptionDe;
    @Column(name = "description_uk", nullable = false, columnDefinition = "text")
    private String descriptionUk;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<ProjectImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<ProjectGoal> goals = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<ProjectActivity> activities = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    private List<ProjectPartner> partners = new ArrayList<>();

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
    public void setSlug(String slug) { this.slug = slug; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public String getCoverImage() { return coverImage; }
    public void setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public String getTitleEn() { return titleEn; }
    public void setTitleEn(String titleEn) { this.titleEn = titleEn; }
    public String getTitleDe() { return titleDe; }
    public void setTitleDe(String titleDe) { this.titleDe = titleDe; }
    public String getTitleUk() { return titleUk; }
    public void setTitleUk(String titleUk) { this.titleUk = titleUk; }

    public String getSubtitleEn() { return subtitleEn; }
    public void setSubtitleEn(String subtitleEn) { this.subtitleEn = subtitleEn; }
    public String getSubtitleDe() { return subtitleDe; }
    public void setSubtitleDe(String subtitleDe) { this.subtitleDe = subtitleDe; }
    public String getSubtitleUk() { return subtitleUk; }
    public void setSubtitleUk(String subtitleUk) { this.subtitleUk = subtitleUk; }

    public String getDescriptionEn() { return descriptionEn; }
    public void setDescriptionEn(String descriptionEn) { this.descriptionEn = descriptionEn; }
    public String getDescriptionDe() { return descriptionDe; }
    public void setDescriptionDe(String descriptionDe) { this.descriptionDe = descriptionDe; }
    public String getDescriptionUk() { return descriptionUk; }
    public void setDescriptionUk(String descriptionUk) { this.descriptionUk = descriptionUk; }

    public List<ProjectImage> getImages() { return images; }
    public List<ProjectGoal> getGoals() { return goals; }
    public List<ProjectActivity> getActivities() { return activities; }
    public List<ProjectPartner> getPartners() { return partners; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void addImage(ProjectImage img) {
        img.setProject(this);
        this.images.add(img);
    }

    public void clearImages() {
        this.images.clear();
    }

    public void addGoal(ProjectGoal g) {
        g.setProject(this);
        this.goals.add(g);
    }

    public void clearGoals() {
        this.goals.clear();
    }

    public void addActivity(ProjectActivity a) {
        a.setProject(this);
        this.activities.add(a);
    }

    public void clearActivities() {
        this.activities.clear();
    }

    public void addPartner(ProjectPartner p) {
        p.setProject(this);
        this.partners.add(p);
    }

    public void clearPartners() {
        this.partners.clear();
    }
}
