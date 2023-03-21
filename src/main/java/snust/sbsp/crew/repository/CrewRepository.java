package snust.sbsp.crew.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import snust.sbsp.crew.domain.Crew;

import java.util.Optional;

public interface CrewRepository extends JpaRepository<Crew, Long> {
  Optional<Crew> findByEmail(String email);
}
