package de.ukrokultur.ukrokultur_api.projects;

import jakarta.persistence.*;

@Entity
@Table(
        name = "project_translation",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "lang"})
)
public class ProjectTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="project_id", nullable = false)
    private Project project;

    @Column(nullable = false, length = 5)
    private String lang; // en | de | uk

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    public Long getId() { return id; }
    public Project getProject() { return project; }
    public String getLang() { return lang; }
    public String getTitle() { return title; }
    public String getText() { return text; }

    public void setProject(Project project) { this.project = project; }
    public void setLang(String lang) { this.lang = lang; }
    public void setTitle(String title) { this.title = title; }
    public void setText(String text) { this.text = text; }
}
