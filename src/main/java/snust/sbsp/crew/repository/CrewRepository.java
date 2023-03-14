package snust.sbsp.crew.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import snust.sbsp.crew.domain.Crew;

public interface CrewRepository extends JpaRepository<Crew, Long> {
}
