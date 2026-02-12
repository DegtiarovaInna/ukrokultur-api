package de.ukrokultur.ukrokultur_api.home;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HomeWorkFieldRepository extends JpaRepository<HomeWorkFieldItem, String> {
    List<HomeWorkFieldItem> findAllByOrderBySortOrderAsc();
}
