package de.ukrokultur.ukrokultur_api.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    @Query("select (count(u) > 0) from User u where u.email = :email")
    boolean existsByEmail(@Param("email") String email);
}
