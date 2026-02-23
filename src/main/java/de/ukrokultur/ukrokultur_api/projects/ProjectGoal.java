package de.ukrokultur.ukrokultur_api.projects;


import jakarta.persistence.*;

@Entity
@Table(name = "project_goal")
public class ProjectGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="project_id", nullable = false)
    private Project project;

    @Column(name="sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name = "en", nullable = false, columnDefinition = "text")
    private String en;

    @Column(name = "de", nullable = false, columnDefinition = "text")
    private String de;

    @Column(name = "uk", nullable = false, columnDefinition = "text")
    private String uk;

    public Long getId() { return id; }

    public Project getProject() { return project; }
    public void setProject(Project project) { this.project = project; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public String getEn() { return en; }
    public void setEn(String en) { this.en = en; }

    public String getDe() { return de; }
    public void setDe(String de) { this.de = de; }

    public String getUk() { return uk; }
    public void setUk(String uk) { this.uk = uk; }
}