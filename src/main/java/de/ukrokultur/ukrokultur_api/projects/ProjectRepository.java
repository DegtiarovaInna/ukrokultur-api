package de.ukrokultur.ukrokultur_api.projects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    boolean existsBySlug(String slug);

    Optional<Project> findBySlug(String slug);

    Optional<Project> findByPublicId(UUID publicId);

    @Query("""
            select p
            from Project p
            where (:publishedOnly = false or p.published = true)
            order by p.createdAt desc
            """)
    Page<Project> findPageOrdered(boolean publishedOnly, Pageable pageable);

    @Query("select coalesce(max(p.sortOrder), -1) from Project p")
    int findMaxSortOrder();
}