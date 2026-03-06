package de.ukrokultur.ukrokultur_api.about;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;
import java.util.UUID;
@Entity
@Table(name = "about_member")
public class AboutMember {

    @Id
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "slug", nullable = false, unique = true, length = 120)
    private String slug;
    @Column(nullable = false, length = 200)
    private String name;

    private String image;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean published = true;

    private String instagramUrl;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "en", column = @Column(name = "role_en", columnDefinition = "text")),
            @AttributeOverride(name = "de", column = @Column(name = "role_de", columnDefinition = "text")),
            @AttributeOverride(name = "uk", column = @Column(name = "role_uk", columnDefinition = "text"))
    })
    private I18nEmbeddable role;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "en", column = @Column(name = "biography_en", columnDefinition = "text")),
            @AttributeOverride(name = "de", column = @Column(name = "biography_de", columnDefinition = "text")),
            @AttributeOverride(name = "uk", column = @Column(name = "biography_uk", columnDefinition = "text"))
    })
    private I18nEmbeddable biography;
    public UUID getId() { return id; }
    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    public String getInstagramUrl() { return instagramUrl; }
    public void setInstagramUrl(String instagramUrl) { this.instagramUrl = instagramUrl; }

    public I18nEmbeddable getRole() { return role; }
    public void setRole(I18nEmbeddable role) { this.role = role; }

    public I18nEmbeddable getBiography() { return biography; }
    public void setBiography(I18nEmbeddable biography) { this.biography = biography; }
}
