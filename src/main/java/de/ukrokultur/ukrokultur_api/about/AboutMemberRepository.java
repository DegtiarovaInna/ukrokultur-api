package de.ukrokultur.ukrokultur_api.about;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

public interface AboutMemberRepository extends JpaRepository<AboutMember, UUID> {
    boolean existsBySlug(String slug);
    Optional<AboutMember> findBySlug(String slug);
    default List<AboutMember> findAllOrdered() {
        return findAll(Sort.by(Sort.Direction.ASC, "order", "slug"));
    }
}
