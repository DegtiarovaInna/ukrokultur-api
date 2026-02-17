package de.ukrokultur.ukrokultur_api.news;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.UUID;
import java.util.Optional;

public interface NewsRepository extends JpaRepository<News, Long> {

    @Query("""
           select n from News n
           where (:publishedOnly = false or n.published = true)
           order by n.publishedAt desc nulls last, n.createdAt desc
           """)
    Page<News> findPageOrdered(boolean publishedOnly, Pageable pageable);

    boolean existsBySlug(String slug);

    Optional<News> findBySlug(String slug);
    Optional<News> findByPublicId(UUID publicId);
}
