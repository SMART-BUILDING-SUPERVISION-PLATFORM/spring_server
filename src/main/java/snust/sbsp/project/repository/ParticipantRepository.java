package snust.sbsp.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import snust.sbsp.project.domain.Participant;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
}
