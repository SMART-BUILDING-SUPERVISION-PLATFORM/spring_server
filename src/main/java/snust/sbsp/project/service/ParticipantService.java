package snust.sbsp.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;
import snust.sbsp.company.domain.Company;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.service.AuthService;
import snust.sbsp.crew.service.CrewService;
import snust.sbsp.project.domain.Participant;
import snust.sbsp.project.domain.Project;
import snust.sbsp.project.domain.type.Role;
import snust.sbsp.project.dto.req.ParticipantReq;
import snust.sbsp.project.repository.ParticipantRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ParticipantService {

	private final ParticipantRepository participantRepository;

	private final CrewService crewService;

	private final AuthService authService;

	private final ProjectService projectService;

	@Transactional
	public void requestToJoin(
		Long currentCrewId,
		ParticipantReq participantReq
	) {
		Crew currentCrew = crewService.readCrewById(currentCrewId);
		Role initialProjectRoleOfIncomingCrew = authService.isAdmin(currentCrewId)
			? Role.ADMIN
			: Role.PENDING;

		Long targetCrewId = participantReq.getTargetCrewId();
		Long projectId = participantReq.getProjectId();
		Optional<Participant> participant = participantRepository.findByCrewIdAndProjectId(targetCrewId, projectId);
		if (participant.isPresent())
			throw new CustomCommonException(ErrorCode.ALREADY_REGISTERED);

		Project project = projectService.readProjectById(projectId);

		Participant newParticipant = Participant.builder()
			.role(initialProjectRoleOfIncomingCrew)
			.crew(currentCrew)
			.project(project)
			.build();

		participantRepository.save(newParticipant);
	}

	@Transactional
	public void updateRole(
		Long currentCrewId,
		ParticipantReq participantReq
	) {
		Long projectId = participantReq.getProjectId();
		Optional<Participant> currentParticipant = participantRepository.findByCrewIdAndProjectId(
			currentCrewId,
			projectId
		);

		Participant participant = projectService.readParticipantByCrewIdAndProjectId(
			participantReq.getTargetCrewId(),
			projectId
		);

		if (!participant.getRole().equals(Role.MANAGER) || currentParticipant.isEmpty())
			updateRoleByAdmin(currentCrewId, participantReq, participant);
		else
			replaceRole(currentCrewId, participantReq, participant);
	}

	@Transactional
	public void deleteParticipant(
		Long currentCrewId,
		ParticipantReq participantReq
	) {
		Crew currentCrew = crewService.readCrewById(currentCrewId);
		Optional<Participant> currentParticipant = participantRepository.findByCrewIdAndProjectId(currentCrewId, participantReq.getProjectId());

		if (currentParticipant.isEmpty()) {
			if (currentCrew.getRole().equals(snust.sbsp.crew.domain.type.Role.COMPANY_ADMIN))
				deleteParticipantByCa(currentCrew, participantReq);

			if (currentCrew.getRole().equals(snust.sbsp.crew.domain.type.Role.SERVICE_ADMIN))
				participantRepository.delete(readParticipant(participantReq));
		} else {
			Role currentParticipantRole = currentParticipant.get().getRole();
			if (currentParticipantRole.equals(Role.ADMIN) || currentParticipantRole.equals(Role.MANAGER))
				participantRepository.delete(readParticipant(participantReq));
			else
				throw new CustomCommonException(ErrorCode.FORBIDDEN);
		}
	}

	private void deleteParticipantByCa(
		Crew currentCrew,
		ParticipantReq participantReq
	) {
		Company currentCompany = currentCrew.getCompany();
		Company targetCompany = crewService.readCrewById(participantReq.getTargetCrewId()).getCompany();

		if (!currentCrew.getRole().equals(snust.sbsp.crew.domain.type.Role.COMPANY_ADMIN))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		if (!currentCompany.equals(targetCompany))
			throw new CustomCommonException(ErrorCode.DIFF_COMPANY);

		participantRepository.delete(readParticipant(participantReq));
	}

	private Participant readParticipant(ParticipantReq participantReq) {
		return projectService.readParticipantByCrewIdAndProjectId(
			participantReq.getTargetCrewId(),
			participantReq.getProjectId()
		);
	}

	private void updateRoleByAdmin(
		Long currentCrewId,
		ParticipantReq participantReq,
		Participant participant
	) {
		if (!authService.isAdmin(currentCrewId))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		replaceRole(currentCrewId, participantReq, participant);
	}

	private void replaceRole(
		Long currentCrewId,
		ParticipantReq participantReq,
		Participant participant
	) {
		Role targetRole = Role.from(participantReq.getRole());

		if (!authService.isAdmin(currentCrewId) && targetRole.equals(Role.ADMIN))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		participant.update(targetRole);
	}
}
