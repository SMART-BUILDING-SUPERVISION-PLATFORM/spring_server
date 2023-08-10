package snust.sbsp.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;
import snust.sbsp.company.domain.Company;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.domain.type.Role;
import snust.sbsp.crew.dto.res.base.CrewDto;
import snust.sbsp.crew.service.CrewService;
import snust.sbsp.project.domain.Note;
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
		// 작성자 조회.
		Crew writer = crewService.readCrewById(currentCrewId);
		// 작성자 회사 추출
		Company companyOfWiter = writer.getCompany();
		// 작성자 서비스 권한 추출
		Role roleOfWriter = writer.getRole();

		// 프로젝트 아이디 추출
		Long currentProjectId = newNoteReq.getProjectId();
		// 해당 프로젝트 조회.
		Project currentProject = projectService.readProjectById(currentProjectId);
		// 프로젝트 회사 추출
		Company companyOfProject = currentProject.getCompany();

		// 현재 회원을
		participantRepository
			// CrewId와 ProjectId를 통해 조회
			.findByCrewIdAndProjectId(currentCrewId, currentProjectId)
			.ifPresentOrElse(
				// 해당 프로젝트 참여자인 경우
				participant -> {
					// 프로젝트 권한 추출
					ProjectRole projectRoleOfParticipant = participant.getProjectRole();

					// 프로젝트 권한이 ADMIN && MANAGER && EDITABLE이 아닌 경우 throw FORBIDDEN
					if (!projectRoleOfParticipant.equals(ProjectRole.ADMIN) &&
						!projectRoleOfParticipant.equals(ProjectRole.MANAGER) &&
						!projectRoleOfParticipant.equals(ProjectRole.EDITABLE))
						throw new CustomCommonException(ErrorCode.FORBIDDEN);
				}, () -> {
					// 해당 프로젝트 참여자이지 않을 경우

					// 서비스 권한이 SERVICE_ADMIN이 아닌경우
					if (!roleOfWriter.equals(Role.SERVICE_ADMIN) &&
						!roleOfWriter.equals(Role.COMPANY_ADMIN))
						throw new CustomCommonException(ErrorCode.FORBIDDEN);

					// 서비스 권한이 COMPANY_ADMIN인데 프로젝트의 회사와 본인의 회사가 일치하지 않으면 throw DIFF_COMPANY.
					if (roleOfWriter.equals(Role.COMPANY_ADMIN) &&
						!companyOfWiter.equals(companyOfProject))
						throw new CustomCommonException(ErrorCode.DIFF_COMPANY);
				}
			);

		// 새로운 노트 객체 빌드, 저장
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
		// 작성자 조회.
		Crew replier = crewService.readCrewById(currentCrewId);
		// 작성자 회사 추출
		Company companyOfReplier = replier.getCompany();
		// 작성자 서비스 권한 추출
		Role roleOfReplier = replier.getRole();

		// 프로젝트 아이디 추출
		Long currentProjectId = replyNoteReq.getProjectId();
		// 해당 프로젝트 조회.
		Project currentProject = projectService.readProjectById(currentProjectId);
		// 프로젝트 회사 추출
		Company companyOfProject = currentProject.getCompany();

		participantRepository
			.findByCrewIdAndProjectId(currentCrewId, currentProjectId)
			.ifPresentOrElse(
				participant -> {
					// 프로젝트 권한 추출
					ProjectRole projectRoleOfParticipant = participant.getProjectRole();

					// 프로젝트 권한이 ADMIN && MANAGER && EDITABLE이 아닌 경우 throw FORBIDDEN
					if (!projectRoleOfParticipant.equals(ProjectRole.ADMIN) &&
						!projectRoleOfParticipant.equals(ProjectRole.MANAGER) &&
						!projectRoleOfParticipant.equals(ProjectRole.EDITABLE))
						throw new CustomCommonException(ErrorCode.FORBIDDEN);
				},
				() -> {
					// 해당 프로젝트 참여자이지 않을 경우

					// 서비스 권한이 SERVICE_ADMIN이 아닌경우
					if (!roleOfReplier.equals(Role.SERVICE_ADMIN) &&
						!roleOfReplier.equals(Role.COMPANY_ADMIN))
						throw new CustomCommonException(ErrorCode.FORBIDDEN);

					// 서비스 권한이 COMPANY_ADMIN인데 프로젝트의 회사와 본인의 회사가 일치하지 않으면 throw DIFF_COMPANY.
					if (roleOfReplier.equals(Role.COMPANY_ADMIN) &&
						!companyOfReplier.equals(companyOfProject))
						throw new CustomCommonException(ErrorCode.DIFF_COMPANY);
				}
			);

		// 해당 노트 조회
		noteRepository.findById(replyNoteReq.getNoteId())
			.ifPresentOrElse(
				note -> {
					// 노트가 존재할 때
					// 답장이 있으면 throw REPLIER_ALREADY_EXIST
					if (note.getReplier() != null)
						throw new CustomCommonException(ErrorCode.REPLIER_ALREADY_EXIST);

					// 답장이 없으면 업데이트.
					note.update(replier, replyNoteReq.getReply());
				},
				() -> {
					// 노트가 존재하지 않을 때.
					throw new CustomCommonException(ErrorCode.NOTE_NOT_FOUND);
				}
			);
	}

	@Transactional(readOnly = true)
	public List<NoteDto> readNote(
		Long currentCrewId,
		Long projectId,
		String noteType,
		Boolean isNew
	) {
		// 프로젝트에 참여중인지 검증.
		participantRepository
			.findByCrewIdAndProjectId(currentCrewId, projectId)
			.ifPresentOrElse(
				participant -> {
					// 참여중일 경우
					// 프로젝트 권한 추출
					ProjectRole projectRole = participant.getProjectRole();

					// 프로젝트 권한이 PENDING일 때 throw FORBIDDEN.
					if (projectRole.equals(ProjectRole.PENDING))
						throw new CustomCommonException(ErrorCode.FORBIDDEN);
				},
				() -> {
					// 참여중이지 않을 경우 throw PARTICIPANT_NOT_FOUND
					throw new CustomCommonException(ErrorCode.PARTICIPANT_NOT_FOUND);
				}
			);

		if (isNew)
			// 답변이 등록되지 않은 노트만 반환.
			return readNoteExceptReplied(projectId);
		else
			// 답변등록 상관없이 모든 노트 반환.
			return readAllNotes(projectId, noteType);
	}

	@Transactional(readOnly = true)
	public List<NoteDto> readNoteForSa(
		Long currentCrewId,
		Long projectId,
		String noteType,
		Boolean isNew
	) {
		// 현재회원 서비스 권한 추출
		Role roleOfCurrentCrew = crewService.readCrewById(currentCrewId).getRole();

		// 서비스 권한이 SERVICE_ADMIN, COMPANY_ADMIN이 아닐 경우 throw FORBIDDEN
		if (!roleOfCurrentCrew.equals(Role.SERVICE_ADMIN) &&
			!roleOfCurrentCrew.equals(Role.COMPANY_ADMIN))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		if (isNew)
			// 답변이 등록되지 않은 노트만 반환.
			return readNoteExceptReplied(projectId);
		else
			// 답변등록 상관없이 모든 노트 반환.
			return readAllNotes(projectId, noteType);
	}

	private List<NoteDto> readAllNotes(
		Long projectId,
		String noteType
	) {
		// 모든 노트리스트 반환
		return noteRepository
			.findByProjectIdAndNoteType(projectId, NoteType.toEnum(noteType))
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
		// 답변된 것은 제외한 노트리스트 반환
		return noteRepository.findByProjectIdAndReplier(projectId, null)
			.stream()
			.map(note -> {
				CrewDto writer = new CrewDto(note.getWriter());

				return new NoteDto(note, writer, null);
			})
			.collect(Collectors.toList());
	}
}
