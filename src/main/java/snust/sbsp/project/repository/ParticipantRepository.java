package snust.sbsp.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import snust.sbsp.project.domain.Participant;

import java.util.Optional;

public interface ParticipantRepository extends JpaRepository<Participant, Long> {
    Optional<Participant> findByCrewIdAndProjectId(Long crewId, Long projectId);
}
