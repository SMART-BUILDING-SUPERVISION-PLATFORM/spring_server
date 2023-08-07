package snust.sbsp.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.project.domain.Note;
import snust.sbsp.project.domain.type.NoteType;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
	List<Note> findByProjectIdAndNoteType(Long projectId, NoteType noteType);

	List<Note> findByProjectIdAndReplier(Long ProjectId, Crew replier);
}
