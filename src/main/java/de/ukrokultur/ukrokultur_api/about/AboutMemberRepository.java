package de.ukrokultur.ukrokultur_api.about;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;

import java.util.List;

public interface AboutMemberRepository extends JpaRepository<AboutMember, String> {
    default List<AboutMember> findAllOrdered() {
        return findAll(Sort.by(Sort.Direction.ASC, "order", "id"));
    }
}
