package de.ukrokultur.ukrokultur_api.home;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HomeWorkFieldRepository extends JpaRepository<HomeWorkFieldItem, String> {
    List<HomeWorkFieldItem> findAllByOrderBySortOrderAsc();
    Optional<HomeWorkFieldItem> findByPublicId(UUID publicId);
}
