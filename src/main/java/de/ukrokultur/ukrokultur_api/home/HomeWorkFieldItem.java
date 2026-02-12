package de.ukrokultur.ukrokultur_api.home;

import jakarta.persistence.*;

@Entity
@Table(name = "home_work_field_item")
public class HomeWorkFieldItem {

    @Id
    @Column(length = 120)
    private String id;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean published = true;

    @Column(name = "title_en", nullable = false)
    private String titleEn;
    @Column(name = "title_de", nullable = false)
    private String titleDe;
    @Column(name = "title_uk", nullable = false)
    private String titleUk;

    @Column(name = "description_en", nullable = false)
    private String descriptionEn;
    @Column(name = "description_de", nullable = false)
    private String descriptionDe;
    @Column(name = "description_uk", nullable = false)
    private String descriptionUk;

    // getters/setters

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }

    public String getTitleEn() { return titleEn; }
    public void setTitleEn(String titleEn) { this.titleEn = titleEn; }
    public String getTitleDe() { return titleDe; }
    public void setTitleDe(String titleDe) { this.titleDe = titleDe; }
    public String getTitleUk() { return titleUk; }
    public void setTitleUk(String titleUk) { this.titleUk = titleUk; }

    public String getDescriptionEn() { return descriptionEn; }
    public void setDescriptionEn(String descriptionEn) { this.descriptionEn = descriptionEn; }
    public String getDescriptionDe() { return descriptionDe; }
    public void setDescriptionDe(String descriptionDe) { this.descriptionDe = descriptionDe; }
    public String getDescriptionUk() { return descriptionUk; }
    public void setDescriptionUk(String descriptionUk) { this.descriptionUk = descriptionUk; }
}
