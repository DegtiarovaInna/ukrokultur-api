package de.ukrokultur.ukrokultur_api.news;

import jakarta.persistence.*;

@Entity
@Table(name = "news_image")
public class NewsImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "news_id", nullable = false)
    private News news;

    @Column(nullable = false, columnDefinition = "text")
    private String url;

    @Column(name="sort_order", nullable = false)
    private int sortOrder = 0;

    @Column(name="is_cover", nullable = false)
    private boolean cover = false;

    public Long getId() { return id; }
    public News getNews() { return news; }
    public String getUrl() { return url; }
    public int getSortOrder() { return sortOrder; }
    public boolean isCover() { return cover; }

    public void setNews(News news) { this.news = news; }
    public void setUrl(String url) { this.url = url; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public void setCover(boolean cover) { this.cover = cover; }
}
