package snust.sbsp.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.domain.type.Role;
import snust.sbsp.crew.dto.res.base.CrewDto;
import snust.sbsp.crew.service.CrewService;
import snust.sbsp.project.domain.Note;
import snust.sbsp.project.domain.Participant;
import snust.sbsp.project.domain.Project;
import snust.sbsp.project.domain.type.NoteType;
import snust.sbsp.project.domain.type.ProjectRole;
import snust.sbsp.project.dto.req.NewNoteReq;
import snust.sbsp.project.dto.req.ReplyNoteReq;
import snust.sbsp.project.dto.res.base.NoteDto;
import snust.sbsp.project.repository.NoteRepository;
import snust.sbsp.project.repository.ParticipantRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoteService {

	private final CrewService crewService;

	private final ProjectService projectService;

	private final NoteRepository noteRepository;

	private final ParticipantRepository participantRepository;


	@Transactional
	public void addNote(
		Long currentCrewId,
		NewNoteReq newNoteReq
	) {
		Crew writer = crewService.readCrewById(currentCrewId);
		Long currentProjectId = newNoteReq.getProjectId();

		if (!writer.getRole().equals(Role.SERVICE_ADMIN) && !writer.getRole().equals(Role.COMPANY_ADMIN)) {
			Participant participant = participantRepository.findByCrewIdAndProjectId(currentCrewId, currentProjectId)
				.orElseThrow(() -> new CustomCommonException(ErrorCode.FORBIDDEN));

			if (!participant.getProjectRole().equals(ProjectRole.MANAGER) &&
				!participant.getProjectRole().equals(ProjectRole.EDITABLE))
				throw new CustomCommonException(ErrorCode.FORBIDDEN);
		}

		Project currentProject = projectService.readProjectById(currentProjectId);

		Note newNote = Note.builder()
			.project(currentProject)
			.noteType(NoteType.toEnum(newNoteReq.getNoteType()))
			.content(newNoteReq.getContent())
			.reply("")
			.writer(writer)
			.replier(null)
			.build();

		noteRepository.save(newNote);
	}

	@Transactional
	public void updateNote(
		Long currentCrewId,
		ReplyNoteReq replyNoteReq
	) {
		Crew currentCrew = crewService.readCrewById(currentCrewId);

		if (!currentCrew.getRole().equals(Role.SERVICE_ADMIN) &&
			!currentCrew.getRole().equals(Role.COMPANY_ADMIN)) {
			Participant participant = participantRepository.findByCrewIdAndProjectId(currentCrewId, replyNoteReq.getProjectId())
				.orElseThrow(() -> new CustomCommonException(ErrorCode.PARTICIPANT_NOT_FOUND));

			if (!participant.getProjectRole().equals(ProjectRole.MANAGER) &&
				!participant.getProjectRole().equals(ProjectRole.EDITABLE))
				throw new CustomCommonException(ErrorCode.FORBIDDEN);
		}

		Note note = noteRepository.findById(replyNoteReq.getNoteId())
			.orElseThrow(() -> new CustomCommonException(ErrorCode.NOTE_NOT_FOUND));

		if (note.getReplier() != null)
			throw new CustomCommonException(ErrorCode.REPLIER_ALREADY_EXIST);

		note.update(currentCrew, replyNoteReq.getReply());
	}

	@Transactional(readOnly = true)
	public List<NoteDto> readNote(
		Long currentCrewId,
		Long projectId,
		String noteType,
		Boolean isNew
	) {
		Participant participant = participantRepository.findByCrewIdAndProjectId(currentCrewId, projectId)
			.orElseThrow(() -> new CustomCommonException(ErrorCode.PARTICIPANT_NOT_FOUND));

		if (participant.getProjectRole().equals(ProjectRole.PENDING))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		if (isNew)
			return readNoteExceptReplied(projectId);
		else
			return readAllNotes(projectId, noteType);
	}

	@Transactional(readOnly = true)
	public List<NoteDto> readNoteForSa(
		Long currentCrewId,
		Long projectId,
		String noteType,
		Boolean isNew
	) {
		Crew currentCrew = crewService.readCrewById(currentCrewId);

		if (!currentCrew.getRole().equals(Role.SERVICE_ADMIN) && !currentCrew.getRole().equals(Role.COMPANY_ADMIN))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		if (isNew)
			return readNoteExceptReplied(projectId);
		else
			return readAllNotes(projectId, noteType);
	}

	private List<NoteDto> readAllNotes(
		Long projectId,
		String noteType
	) {
		return noteRepository.findByProjectIdAndNoteType(projectId, NoteType.toEnum(noteType))
			.stream()
			.map(note -> {
					CrewDto writerDto = new CrewDto(note.getWriter());
					Crew replier = note.getReplier();

					if (replier == null)
						return new NoteDto(note, writerDto, null);
					else
						return new NoteDto(note, writerDto, new CrewDto(replier));
				}
			)
			.collect(Collectors.toList());
	}

	private List<NoteDto> readNoteExceptReplied(
		Long projectId
	) {
		return noteRepository.findByProjectIdAndReplier(projectId, null)
			.stream()
			.map(note -> {
				CrewDto writer = new CrewDto(note.getWriter());

				return new NoteDto(note, writer, null);
			})
			.collect(Collectors.toList());
	}
}
