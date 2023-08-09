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
		Long projectId
	) {
		// ProjectRole Initialize
		ProjectRole initProjectRole = ProjectRole.PENDING;

		// 현재 회원 조회.
		Crew currentCrew = crewService.readCrewById(currentCrewId);
		// 현재 회원의 권한 조회.
		Role roleOfCurrentCrew = currentCrew.getRole();
		// 현재 회원의 회사 조회.
		Company companyOfCurrentCrew = currentCrew.getCompany();

		// 해당 프로젝트 조회.
		Project project = projectService.readProjectById(projectId);
		// 해당 프로젝트의 회사 조회.
		Company companyOfProject = project.getCompany();

		// ProjectRole ReInitialize (ProjectRole -> ADMIN)
		// 아래에 해당하지 않는 경우는 모두 (ProjectRole -> PENDING)
		// 현재 회원의 권한이 SERVICE_ADMIN인 경우
		if (roleOfCurrentCrew.equals(Role.SERVICE_ADMIN))
			initProjectRole = ProjectRole.ADMIN;
			// 현재 회원의 권한이 COMPANY_ADMIN이고, 해당 프로젝트의 회사와 동일한 회사일 때
		else if (roleOfCurrentCrew.equals(Role.COMPANY_ADMIN) &&
			companyOfCurrentCrew.equals(companyOfProject))
			initProjectRole = ProjectRole.ADMIN;

		// 해당 프로젝트에 이미 참여자로 등록이 되어있는지 확인.
		Optional<Participant> participant = participantRepository.findByCrewIdAndProjectId(currentCrewId, projectId);
		// 이미 등록되어 있을 경우 throw ALREADY_REGISTERD.
		if (participant.isPresent())
			throw new CustomCommonException(ErrorCode.ALREADY_REGISTERED);

		// Participant Entity로 변환.
		Participant newParticipant = Participant.builder()
			.projectRole(initProjectRole)
			.crew(currentCrew)
			.project(project)
			.build();

		// Participant Table에 저장.
		participantRepository.save(newParticipant);
	}

	@Transactional
	public void updateRole(
		Long currentCrewId,
		UpdateParticipantRoleReq updateParticipantRoleReq
	) {
		// 현재 ProjectID 추출.
		Long projectId = updateParticipantRoleReq.getProjectId();
		// 권한변경을 적용할 대상 CrewID 추출.
		Long targetCrewId = updateParticipantRoleReq.getTargetCrewId();


		// 해당 프로젝트에 현재 회원이 참여중인지 검사.
		Optional<Participant> currentParticipant = participantRepository
			.findByCrewIdAndProjectId(currentCrewId, projectId);

		// 권한변경 대상 회원이 해당 프로젝트의 참여자인지 검사.
		Participant tartgetParticipant = projectService
			.readParticipantByCrewIdAndProjectId(targetCrewId, projectId);

		// 현재 회원이 해당 프로젝트의 참여자일 경우
		currentParticipant.ifPresent(
			participant -> {
				// 현재 참여자의 현재 프로젝트 내에서의 권한 조회.
				ProjectRole projectRoleOfParticipant = participant.getProjectRole();

				// 현재 참여자의 프로젝트 권한이 ADMIN도 아니고 MANAGER도 아닐 떄 throw FORBIDDEN.
				if (!projectRoleOfParticipant.equals(ProjectRole.ADMIN) &&
					!projectRoleOfParticipant.equals(ProjectRole.MANAGER)) {
					throw new CustomCommonException(ErrorCode.FORBIDDEN);
				} else {
					if (currentCrewId.equals(targetCrewId))
						throw new CustomCommonException(ErrorCode.POINT_ITSELF);
					// 현재 참여자의 프로젝트 권한이 ADMIN또는 MANAGER일 때
					replaceRole(participant, tartgetParticipant, updateParticipantRoleReq);
				}
			});

		// 현재 회원이 해당 프로젝트의 참여자가 아닐 경우 (관리자로 접근.)
		if (currentParticipant.isEmpty()) {
			if (currentCrewId.equals(targetCrewId))
				throw new CustomCommonException(ErrorCode.POINT_ITSELF);
			updateRoleByAdmin(currentCrewId, updateParticipantRoleReq, tartgetParticipant);
		}
	}

	@Transactional
	public void deleteParticipant(
		Long currentCrewId,
		DeleteParticipantReq deleteParticipantReq
	) {
		// 현재 회원 정보조회.
		Crew currentCrew = crewService.readCrewById(currentCrewId);
		// 현재 회원 서비스 권한 조회
		Role roleOfCurrentCrew = currentCrew.getRole();

		// 삭제 대상 CrewId
		Long targetCerwId = deleteParticipantReq.getTargetCrewId();

		// 현재 회원이 해당 프로젝트애 참여중인지 검증.
		Optional<Participant> currentParticipant = participantRepository.findByCrewIdAndProjectId(currentCrewId, deleteParticipantReq.getProjectId());

		// 해당 프로젝트에 참여하고 있을 경우
		currentParticipant.ifPresent(
			participant -> {
				// 해당 참여자의 프로젝트 권한 추출.
				ProjectRole projectRoleOfCurrentParticipant = participant.getProjectRole();

				// 프로젝트 권한이 ADMIN도 아니고 MANAGER도 아닌 경우. throw FORBIDDEN.
				if (!projectRoleOfCurrentParticipant.equals(ProjectRole.ADMIN)
					&& !projectRoleOfCurrentParticipant.equals(ProjectRole.MANAGER))
					throw new CustomCommonException(ErrorCode.FORBIDDEN);

				if (currentCrewId.equals(targetCerwId))
					throw new CustomCommonException(ErrorCode.POINT_ITSELF);
			}
		);

		// 해당 프로젝트에 참여하고 있지 않을 경우.
		if (currentParticipant.isEmpty()) {
			// 현재 회원의 서비스 권한이 COMPANY_ADMIN일 경우
			if (roleOfCurrentCrew.equals(snust.sbsp.crew.domain.type.Role.COMPANY_ADMIN))
				deleteParticipantByCa(currentCrew, deleteParticipantReq);

			// 현재 회원의 서비스 권한이 SERVICE_ADMIN일 경우
			if (roleOfCurrentCrew.equals(snust.sbsp.crew.domain.type.Role.SERVICE_ADMIN)) {
				if (currentCrewId.equals(targetCerwId))
					throw new CustomCommonException(ErrorCode.POINT_ITSELF);
				participantRepository.delete(readParticipantForDelete(deleteParticipantReq));
			}
		}
	}

	private Participant readParticipantForDelete(DeleteParticipantReq deleteParticipantReq) {
		return projectService.readParticipantByCrewIdAndProjectId(
			deleteParticipantReq.getTargetCrewId(),
			deleteParticipantReq.getProjectId()
		);
	}

	private void updateRoleByAdmin(
		Long currentCrewId,
		UpdateParticipantRoleReq updateParticipantRoleReq,
		Participant targetParticipant
	) {
		// 현재 회원 조회
		Crew currentCrew = crewService.readCrewById(currentCrewId);
		// 현재 회원의 회사 조회
		Company companyOfCurrentCrew = currentCrew.getCompany();
		// 해당 프로젝트의 회사 조회
		Company companyOfCurrentProject = projectService.readProjectById(updateParticipantRoleReq.getProjectId()).getCompany();

		// 현재 회원의 서비스 권한이 COMPANY_ADMIN이지만 프로젝트의 회사와 다른 회사일 때
		// throw FORBIDDEN.
		if (currentCrew.getRole().equals(Role.COMPANY_ADMIN) &&
			!companyOfCurrentCrew.equals(companyOfCurrentProject))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		// 업데이트 하고자 하는 ProjectRole 추출.
		ProjectRole targetProjectRole = ProjectRole.toEnum(updateParticipantRoleReq.getTargetProjectRole());

		// 현재 회원이 (SERVICE, COMPANY)_ADMIN가 아니고, MANAGER도 아니면
		// throw FORBIDDEN.
		if (!authService.isAdmin(currentCrewId) && !targetProjectRole.equals(ProjectRole.MANAGER))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		// 대상 회원 프로젝트 권한 업데이트
		targetParticipant.update(targetProjectRole);
	}

	private void deleteParticipantByCa(
		Crew currentCrew,
		DeleteParticipantReq deleteParticipantReq
	) {
		// 현재 회원의 회사정보 조회.
		Company companyOfCurrentCrew = currentCrew.getCompany();
		// 해당 프로젝트의 회사정보 조회.
		Company companyOfCurrentProject = projectService.readProjectById(deleteParticipantReq.getProjectId()).getCompany();

		// 현재 회원의 서비스 권한이 COMPANY_ADMIN이 아닌 경우. throw FORBIDDEN
		if (!currentCrew.getRole().equals(snust.sbsp.crew.domain.type.Role.COMPANY_ADMIN))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		// 현재 회원의 회사정보와 프로젝트의 회사정보가 일치하지 않으면 throw FORBIDDEN
		if (!companyOfCurrentCrew.equals(companyOfCurrentProject))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		// 삭제 대상 PARTICIPANT ENTITY추출
		Participant participantForDelete = readParticipantForDelete(deleteParticipantReq);

		Long currentCrewId = currentCrew.getId();
		Long targetCerwId = deleteParticipantReq.getTargetCrewId();
		if (currentCrewId.equals(targetCerwId))
			throw new CustomCommonException(ErrorCode.POINT_ITSELF);

		// 참여자 삭제.
		participantRepository.delete(participantForDelete);
	}

	private void replaceRole(
		Participant currentParticipant,
		Participant targetParticipant,
		UpdateParticipantRoleReq updateParticipantRoleReq
	) {
		// 현재 참여자의 프로젝트 권한 추출
		ProjectRole projectRoleOfCurrentParticipant = currentParticipant.getProjectRole();

		// 현재 참여자의 프로젝트 권한이 MANAGER도 ADMIN도 아닌경우 throw FORBIDDEN.
		if (!projectRoleOfCurrentParticipant.equals(ProjectRole.MANAGER) &&
			!projectRoleOfCurrentParticipant.equals(ProjectRole.ADMIN))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		// 변경 대상 프로젝트 권한 추출.
		ProjectRole targetProjectRole = ProjectRole.toEnum(updateParticipantRoleReq.getTargetProjectRole());

		// 참여자 권한 변경.
		targetParticipant.update(targetProjectRole);
	}
}
