package de.ukrokultur.ukrokultur_api.about;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "about_intro")
public class AboutIntro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String image;

    @Embedded
    private I18nEmbeddable title;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "en", column = @Column(name = "text_en", columnDefinition = "text")),
            @AttributeOverride(name = "de", column = @Column(name = "text_de", columnDefinition = "text")),
            @AttributeOverride(name = "uk", column = @Column(name = "text_uk", columnDefinition = "text"))
    })
    private I18nEmbeddable text;

    @Column(nullable = false)
    private boolean published = true;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }


    public Long getId() { return id; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public I18nEmbeddable getTitle() { return title; }
    public void setTitle(I18nEmbeddable title) { this.title = title; }

    public I18nEmbeddable getText() { return text; }
    public void setText(I18nEmbeddable text) { this.text = text; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
