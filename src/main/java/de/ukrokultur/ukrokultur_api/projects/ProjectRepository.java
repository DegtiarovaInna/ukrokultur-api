package de.ukrokultur.ukrokultur_api.projects;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    boolean existsBySlug(String slug);

    java.util.Optional<Project> findBySlug(String slug);

    @Query("""
           select p from Project p
           where (:publishedOnly = false or p.published = true)
           order by p.createdAt desc
           """)
    List<Project> findAllOrdered(boolean publishedOnly);
}
