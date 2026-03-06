package de.ukrokultur.ukrokultur_api.repository;

import de.ukrokultur.ukrokultur_api.news.News;
import de.ukrokultur.ukrokultur_api.news.NewsRepository;
import de.ukrokultur.ukrokultur_api.support.AbstractJpaTcTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


class NewsRepositoryTest extends AbstractJpaTcTest{

    @Autowired
    private NewsRepository repo;

    @Test
    void findPageOrdered_ordersByPublishedAtDescNullsLast_thenCreatedAt() {
        News n1 = new News();
        n1.setPublished(true);
        n1.setSlug("a");
        n1.setPublishedAt(OffsetDateTime.of(2025, 6, 20, 0, 0, 0, 0, ZoneOffset.UTC));
        repo.save(n1);

        News n2 = new News();
        n2.setPublished(true);
        n2.setSlug("b");
        n2.setPublishedAt(OffsetDateTime.of(2025, 6, 21, 0, 0, 0, 0, ZoneOffset.UTC));
        repo.save(n2);

        News n3 = new News();
        n3.setPublished(true);
        n3.setSlug("c");
        n3.setPublishedAt(null);
        repo.saveAndFlush(n3);

        var page = repo.findPageOrdered(true, PageRequest.of(0, 10));
        assertThat(page.getContent()).extracting(News::getSlug).containsExactly("b", "a", "c");
    }

    @Test
    void findByPublicId_works() {
        News n = new News();
        n.setPublished(true);
        n.setSlug("x");
        n.setPublicId(UUID.randomUUID());
        repo.saveAndFlush(n);

        assertThat(repo.findByPublicId(n.getPublicId())).isPresent();
    }
}