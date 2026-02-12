package de.ukrokultur.ukrokultur_api.home;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "home_page")
public class HomePage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hero_image")
    private String heroImage;

    @Column(name = "hero_title_en", nullable = false)
    private String heroTitleEn;
    @Column(name = "hero_title_de", nullable = false)
    private String heroTitleDe;
    @Column(name = "hero_title_uk", nullable = false)
    private String heroTitleUk;

    @Column(name = "hero_subtitle_en", nullable = false)
    private String heroSubtitleEn;
    @Column(name = "hero_subtitle_de", nullable = false)
    private String heroSubtitleDe;
    @Column(name = "hero_subtitle_uk", nullable = false)
    private String heroSubtitleUk;

    @Column(name = "hero_published", nullable = false)
    private boolean heroPublished = true;

    @Column(name = "mission_image")
    private String missionImage;

    @Column(name = "mission_title_en", nullable = false)
    private String missionTitleEn;
    @Column(name = "mission_title_de", nullable = false)
    private String missionTitleDe;
    @Column(name = "mission_title_uk", nullable = false)
    private String missionTitleUk;

    @Column(name = "mission_text_en", nullable = false)
    private String missionTextEn;
    @Column(name = "mission_text_de", nullable = false)
    private String missionTextDe;
    @Column(name = "mission_text_uk", nullable = false)
    private String missionTextUk;

    @Column(name = "mission_published", nullable = false)
    private boolean missionPublished = true;

    @Column(name = "work_fields_published", nullable = false)
    private boolean workFieldsPublished = true;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void touchUpdatedAt() {
        this.updatedAt = OffsetDateTime.now();
    }


    public Long getId() { return id; }

    public String getHeroImage() { return heroImage; }
    public void setHeroImage(String heroImage) { this.heroImage = heroImage; }

    public String getHeroTitleEn() { return heroTitleEn; }
    public void setHeroTitleEn(String heroTitleEn) { this.heroTitleEn = heroTitleEn; }
    public String getHeroTitleDe() { return heroTitleDe; }
    public void setHeroTitleDe(String heroTitleDe) { this.heroTitleDe = heroTitleDe; }
    public String getHeroTitleUk() { return heroTitleUk; }
    public void setHeroTitleUk(String heroTitleUk) { this.heroTitleUk = heroTitleUk; }

    public String getHeroSubtitleEn() { return heroSubtitleEn; }
    public void setHeroSubtitleEn(String heroSubtitleEn) { this.heroSubtitleEn = heroSubtitleEn; }
    public String getHeroSubtitleDe() { return heroSubtitleDe; }
    public void setHeroSubtitleDe(String heroSubtitleDe) { this.heroSubtitleDe = heroSubtitleDe; }
    public String getHeroSubtitleUk() { return heroSubtitleUk; }
    public void setHeroSubtitleUk(String heroSubtitleUk) { this.heroSubtitleUk = heroSubtitleUk; }

    public boolean isHeroPublished() { return heroPublished; }
    public void setHeroPublished(boolean heroPublished) { this.heroPublished = heroPublished; }

    public String getMissionImage() { return missionImage; }
    public void setMissionImage(String missionImage) { this.missionImage = missionImage; }

    public String getMissionTitleEn() { return missionTitleEn; }
    public void setMissionTitleEn(String missionTitleEn) { this.missionTitleEn = missionTitleEn; }
    public String getMissionTitleDe() { return missionTitleDe; }
    public void setMissionTitleDe(String missionTitleDe) { this.missionTitleDe = missionTitleDe; }
    public String getMissionTitleUk() { return missionTitleUk; }
    public void setMissionTitleUk(String missionTitleUk) { this.missionTitleUk = missionTitleUk; }

    public String getMissionTextEn() { return missionTextEn; }
    public void setMissionTextEn(String missionTextEn) { this.missionTextEn = missionTextEn; }
    public String getMissionTextDe() { return missionTextDe; }
    public void setMissionTextDe(String missionTextDe) { this.missionTextDe = missionTextDe; }
    public String getMissionTextUk() { return missionTextUk; }
    public void setMissionTextUk(String missionTextUk) { this.missionTextUk = missionTextUk; }

    public boolean isMissionPublished() { return missionPublished; }
    public void setMissionPublished(boolean missionPublished) { this.missionPublished = missionPublished; }

    public boolean isWorkFieldsPublished() { return workFieldsPublished; }
    public void setWorkFieldsPublished(boolean workFieldsPublished) { this.workFieldsPublished = workFieldsPublished; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
