package de.ukrokultur.ukrokultur_api.projects;


import jakarta.persistence.*;

@Entity
@Table(name = "project_image")
public class ProjectImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="project_id", nullable = false)
    private Project project;

    @Column(nullable = false, columnDefinition = "text")
    private String url;

    @Column(name="sort_order", nullable = false)
    private int sortOrder = 0;

    public Long getId() { return id; }
    public Project getProject() { return project; }
    public String getUrl() { return url; }
    public int getSortOrder() { return sortOrder; }

    public void setProject(Project project) { this.project = project; }
    public void setUrl(String url) { this.url = url; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
}
