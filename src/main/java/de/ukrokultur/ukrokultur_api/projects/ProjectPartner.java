package de.ukrokultur.ukrokultur_api.projects;

import jakarta.persistence.*;

@Entity
@Table(name = "project_partner")
public class ProjectPartner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="project_id", nullable = false)
    private Project project;

    @Column(name="sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "country_en", nullable = false, columnDefinition = "text")
    private String countryEn;

    @Column(name = "country_de", nullable = false, columnDefinition = "text")
    private String countryDe;

    @Column(name = "country_uk", nullable = false, columnDefinition = "text")
    private String countryUk;

    @Column(name = "organization", nullable = false, length = 500)
    private String organization;

    public Long getId() { return id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public String getCountryEn() { return countryEn; }
    public void setCountryEn(String countryEn) { this.countryEn = countryEn; }

    public String getCountryDe() { return countryDe; }
    public void setCountryDe(String countryDe) { this.countryDe = countryDe; }

    public String getCountryUk() { return countryUk; }
    public void setCountryUk(String countryUk) { this.countryUk = countryUk; }

    public String getOrganization() { return organization; }
    public void setOrganization(String organization) { this.organization = organization; }
}