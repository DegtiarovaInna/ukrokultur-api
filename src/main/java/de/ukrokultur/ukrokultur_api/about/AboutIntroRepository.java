package de.ukrokultur.ukrokultur_api.about;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AboutIntroRepository extends JpaRepository<AboutIntro, Long> {
    Optional<AboutIntro> findTopByOrderByUpdatedAtDesc();
}
