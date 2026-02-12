package de.ukrokultur.ukrokultur_api.news;

import jakarta.persistence.*;

@Entity
@Table(
        name = "news_translation",
        uniqueConstraints = @UniqueConstraint(columnNames = {"news_id", "lang"})
)
public class NewsTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", nullable = false)
    private News news;

    @Column(nullable = false, length = 5)
    private String lang; // "en" | "de" | "uk"

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    public Long getId() { return id; }
    public News getNews() { return news; }
    public String getLang() { return lang; }
    public String getTitle() { return title; }
    public String getText() { return text; }

    public void setNews(News news) { this.news = news; }
    public void setLang(String lang) { this.lang = lang; }
    public void setTitle(String title) { this.title = title; }
    public void setText(String text) { this.text = text; }
}
