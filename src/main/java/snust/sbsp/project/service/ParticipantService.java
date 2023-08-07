package snust.sbsp.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;
import snust.sbsp.company.domain.Company;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.domain.type.Role;
import snust.sbsp.crew.service.AuthService;
import snust.sbsp.crew.service.CrewService;
import snust.sbsp.project.domain.Participant;
import snust.sbsp.project.domain.Project;
import snust.sbsp.project.domain.type.ProjectRole;
import snust.sbsp.project.dto.req.DeleteParticipantReq;
import snust.sbsp.project.dto.req.UpdateParticipantRoleReq;
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
		UpdateParticipantRoleReq updateParticipantRoleReq
	) {
		Crew currentCrew = crewService.readCrewById(currentCrewId);
		ProjectRole initialProjectProjectRoleOfIncomingCrew = authService.isAdmin(currentCrewId)
			? ProjectRole.ADMIN
			: ProjectRole.PENDING;

		Long targetCrewId = updateParticipantRoleReq.getTargetCrewId();
		Long projectId = updateParticipantRoleReq.getProjectId();
		Optional<Participant> participant = participantRepository.findByCrewIdAndProjectId(targetCrewId, projectId);
		if (participant.isPresent())
			throw new CustomCommonException(ErrorCode.ALREADY_REGISTERED);

		Project project = projectService.readProjectById(projectId);

		Participant newParticipant = Participant.builder()
			.projectRole(initialProjectProjectRoleOfIncomingCrew)
			.crew(currentCrew)
			.project(project)
			.build();

		participantRepository.save(newParticipant);
	}

	@Transactional
	public void updateRole(
		Long currentCrewId,
		UpdateParticipantRoleReq updateParticipantRoleReq
	) {
		Long projectId = updateParticipantRoleReq.getProjectId();
		Optional<Participant> currentParticipant = participantRepository.findByCrewIdAndProjectId(
			currentCrewId,
			projectId
		);

		Participant tartgetParticipant = projectService.readParticipantByCrewIdAndProjectId(
			updateParticipantRoleReq.getTargetCrewId(),
			projectId
		);

		currentParticipant.ifPresent(participant ->
		{
			if (!currentParticipant.get().getCrew().getRole().equals(Role.COMPANY_ADMIN) &&
				!participant.getProjectRole().equals(ProjectRole.MANAGER))
				throw new CustomCommonException(ErrorCode.FORBIDDEN);

			else
				replaceRole(currentCrewId, updateParticipantRoleReq, tartgetParticipant);
		});

		if (currentParticipant.isEmpty())
			updateRoleByAdmin(currentCrewId, updateParticipantRoleReq, tartgetParticipant);
	}

	@Transactional
	public void deleteParticipant(
		Long currentCrewId,
		DeleteParticipantReq deleteParticipantReq
	) {
		Crew currentCrew = crewService.readCrewById(currentCrewId);
		Optional<Participant> currentParticipant = participantRepository.findByCrewIdAndProjectId(currentCrewId, deleteParticipantReq.getProjectId());

		if (currentParticipant.isEmpty()) {
			if (currentCrew.getRole().equals(snust.sbsp.crew.domain.type.Role.COMPANY_ADMIN))
				deleteParticipantByCa(currentCrew, deleteParticipantReq);

			if (currentCrew.getRole().equals(snust.sbsp.crew.domain.type.Role.SERVICE_ADMIN))
				participantRepository.delete(readParticipantForDelete(deleteParticipantReq));
		} else {
			ProjectRole currentParticipantProjectRole = currentParticipant.get().getProjectRole();
			Company currentProjectCompany = projectService.readProjectById(deleteParticipantReq.getProjectId()).getCompany();

			if (currentCrew.getRole().equals(Role.COMPANY_ADMIN) &&
				!currentParticipantProjectRole.equals(ProjectRole.MANAGER) &&
				!currentCrew.getCompany().equals(currentProjectCompany))
				throw new CustomCommonException(ErrorCode.FORBIDDEN);

			if (currentCrew.getRole().equals(Role.COMPANY_ADMIN) || currentParticipantProjectRole.equals(ProjectRole.MANAGER))
				participantRepository.delete(readParticipantForDelete(deleteParticipantReq));

			else
				throw new CustomCommonException(ErrorCode.FORBIDDEN);
		}
	}

	private void deleteParticipantByCa(
		Crew currentCrew,
		DeleteParticipantReq deleteParticipantReq
	) {
		Company currentCompany = currentCrew.getCompany();
		Company targetProjectCompany = projectService.readProjectById(deleteParticipantReq.getProjectId()).getCompany();

		if (!currentCrew.getRole().equals(snust.sbsp.crew.domain.type.Role.COMPANY_ADMIN))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		if (!currentCompany.equals(targetProjectCompany))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		participantRepository.delete(readParticipantForDelete(deleteParticipantReq));
	}

	private Participant readParticipantForDelete(DeleteParticipantReq deleteParticipantReq) {
		return projectService.readParticipantByCrewIdAndProjectId(
			deleteParticipantReq.getTargetCrewId(),
			deleteParticipantReq.getProjectId()
		);
	}

	private Participant readParticipantForUpdate(UpdateParticipantRoleReq updateParticipantRoleReq) {
		return projectService.readParticipantByCrewIdAndProjectId(
			updateParticipantRoleReq.getTargetCrewId(),
			updateParticipantRoleReq.getProjectId()
		);
	}

	private void updateRoleByAdmin(
		Long currentCrewId,
		UpdateParticipantRoleReq updateParticipantRoleReq,
		Participant targetParticipant
	) {
		Crew currentCrew = crewService.readCrewById(currentCrewId);
		Company currentCrewCompany = currentCrew.getCompany();
		Company currentProjectCompany = projectService.readProjectById(updateParticipantRoleReq.getProjectId()).getCompany();

		if (currentCrew.getRole().equals(Role.COMPANY_ADMIN) &&
			!currentCrewCompany.equals(currentProjectCompany))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		ProjectRole targetProjectRole = ProjectRole.toEnum(updateParticipantRoleReq.getRole());

		if (!authService.isAdmin(currentCrewId) && !targetProjectRole.equals(ProjectRole.MANAGER))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		targetParticipant.update(targetProjectRole);
	}

	private void replaceRole(
		Long currentCrewId,
		UpdateParticipantRoleReq updateParticipantRoleReq,
		Participant participant
	) {
		ProjectRole targetProjectRole = ProjectRole.toEnum(updateParticipantRoleReq.getRole());

		if (targetProjectRole.equals(ProjectRole.MANAGER))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		participant.update(targetProjectRole);
	}
}
