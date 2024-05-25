package snust.sbsp.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;
import snust.sbsp.common.util.EmailUtil;
import snust.sbsp.company.domain.Company;
import snust.sbsp.company.dto.res.base.CompanyDto;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.domain.type.Role;
import snust.sbsp.crew.service.AuthService;
import snust.sbsp.crew.service.CrewService;
import snust.sbsp.project.domain.Panorama;
import snust.sbsp.project.domain.Participant;
import snust.sbsp.project.domain.Project;
import snust.sbsp.project.domain.type.CtrType;
import snust.sbsp.project.domain.type.DetailCtrType;
import snust.sbsp.project.domain.type.ProjectRole;
import snust.sbsp.project.dto.req.PanoramaReq;
import snust.sbsp.project.dto.req.ProjectReq;
import snust.sbsp.project.dto.res.ProjectRes;
import snust.sbsp.project.dto.res.base.ParticipantDto;
import snust.sbsp.project.repository.PanoramaRepository;
import snust.sbsp.project.repository.ParticipantRepository;
import snust.sbsp.project.repository.ProjectRepository;
import snust.sbsp.project.specification.ProjectSpecification;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

	private final CrewService crewService;

	private final AuthService authService;

	private final EmailUtil emailUtil;

	private final ProjectRepository projectRepository;

	private final PanoramaRepository panoramaRepository;

	private final ParticipantRepository participantRepository;

	@Transactional
	public void createProject(
		ProjectReq projectReq,
		Long currentCrewId
	) {
		// 현재 회원정보 조회.
		Crew crew = crewService.readCrewById(currentCrewId);
		// 현재 회원 권한조회.
		Role role = crew.getRole();

		// 회원의 권한이 SERVICE_ADMIN || COMPANY_ADMIN이 맞는지 검증.
		if (!role.equals(Role.COMPANY_ADMIN) && !role.equals(Role.SERVICE_ADMIN))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		// 회원의 회사조회.
		Company company = crew.getCompany();

		Project project = Project.builder()
			.name(projectReq.getName())
			.startDate(projectReq.getStartDate())
			.endDate(projectReq.getEndDate())
			.ctrType(CtrType.from(projectReq.getCtrType()))
			.detailCtrType(DetailCtrType.from(projectReq.getDetailCtrType()))
			.thumbnailUrl(projectReq.getThumbnailUrl())
			.floorUrl(projectReq.getFloorPlanUrl())
			.company(company)
			.build();

		projectRepository.save(project);
	}

	@Transactional(readOnly = true)
	public List<ProjectRes> readExceptMyProjectList(
		Long currentCrewId,
		Long companyId,
		String name,
		String ctrType,
		String detailCtrType
	) {
		// 다중쿼리 생성
		Specification<Project> specification = ((root, query, criteriaBuilder) -> null);
		if (name != null)
			specification = specification.and(ProjectSpecification.equalName(name));
		if (ctrType != null)
			specification = specification.and(ProjectSpecification.equalCtrType(CtrType.from(ctrType)));
		if (detailCtrType != null)
			specification = specification.and(ProjectSpecification.equalDetailCtrType(DetailCtrType.from(detailCtrType)));
		if (companyId != null) {
			specification = specification.and(ProjectSpecification.equalCompanyId(companyId));
		}

		// 모든 프로젝트를 조회 후 형식변환.(1)
		List<ProjectRes> allProjectList = projectRepository.findAll(specification)
			.stream()
			.map(project -> {
				return new ProjectRes(
					project,
					getParticipantListDto(project.getParticipantList()),
					new CompanyDto(project.getCompany()));
			}).collect(Collectors.toList());

		// 참여하고 있는 프로젝트 조회.(2)
		List<ProjectRes> myProjectList = readMyProjectList(
			currentCrewId,
			companyId,
			name,
			ctrType,
			detailCtrType
		);

		// (1) - (2) = (3)
		allProjectList.removeAll(myProjectList);

		// 승인 대기중인 프로젝트 조회.(4)
		List<ProjectRes> myPendingProjectList = readMyPendingProjectList(
			currentCrewId,
			companyId,
			name,
			ctrType,
			detailCtrType
		);

		// (3) - (4) = (5)
		allProjectList.removeAll(myPendingProjectList);

		// (5) 반환
		return allProjectList;
	}

	@Transactional(readOnly = true)
	public List<ProjectRes> readMyProjectList(
		Long currentCrewId,
		Long companyId,
		String name,
		String ctrType,
		String detailCtrType
	) {
		// 현재 회원 조회
		Crew currentCrew = crewService.readCrewById(currentCrewId);

		// 참여중인 모든 프로젝트 조회
		List<Participant> participantList = currentCrew.getParticipantList();

		// 모든프로젝트 리스트 형식 변환 (1)
		List<ProjectRes> allMyProjectList = convertToProjectRes(
			participantList,
			companyId,
			name,
			ctrType,
			detailCtrType
		);

		// 현재 회원이 참여중인 프로젝트 중 PENDING인 프로젝트만 가져옴. (2)
		List<ProjectRes> myPendingProjectList = readMyPendingProjectList(
			currentCrewId,
			companyId,
			name,
			ctrType,
			detailCtrType);

		// (1) - (2) = (3)
		allMyProjectList.removeAll(myPendingProjectList);

		// (3) 반환.
		return allMyProjectList;
	}

	@Transactional(readOnly = true)
	public List<ProjectRes> readMyPendingProjectList(
		Long currentCrewId,
		Long companyId,
		String name,
		String ctrType,
		String detailCtrType
	) {
		// 현재 회원 조회.
		Crew currentCrew = crewService.readCrewById(currentCrewId);

		// 현재 회원이 참여중인 프로젝트 중 PENDING인 프로젝트만 가져옴.
		List<Participant> participantList = currentCrew.getParticipantList()
			.stream()
			.filter(participant -> participant.getProjectRole().equals(ProjectRole.PENDING))
			.collect(Collectors.toList());

		return convertToProjectRes(
			participantList,
			companyId,
			name,
			ctrType,
			detailCtrType
		);
	}

	@Transactional(readOnly = true)
	public List<ProjectRes> convertToProjectRes(
		List<Participant> participantList,
		Long companyId,
		String name,
		String ctrType,
		String detailCtrType
	) {
		// List<Participant> -> List<ProjectRes>로 변환.
		return participantList
			.stream()
			.filter(
				participant -> {
					Project project = participant.getProject();

					// 프로젝트 쿼리 필터링
					boolean matchesCompanyId = companyId == null || companyId.equals(project.getCompany().getId());
					boolean matchesName = name == null || name.equals(project.getName());
					boolean matchesCtrType = ctrType == null || ctrType.equals(project.getCtrType().getValue());
					boolean matchesDetailCtrType = detailCtrType == null || detailCtrType.equals(project.getDetailCtrType().getValue());

					return matchesCompanyId && matchesName && matchesCtrType && matchesDetailCtrType;
				})
			// ProjectRes로 변환.
			.map(participant ->
				new ProjectRes(
					participant.getProject(),
					getParticipantListDto(participant.getProject().getParticipantList()),
					new CompanyDto(participant.getProject().getCompany()
					)
				))
			.collect(Collectors.toList());
	}

	;

	@Transactional
	public void updateProject(
		Long projectId,
		ProjectReq projectReq,
		Long currentCrewId
	) {
		// 현재 회원 조회
		Crew currentCrew = crewService.readCrewById(currentCrewId);
		Company companyOfCurrentCrew = currentCrew.getCompany();

		// 해당 프로젝트 조회
		Project project = readProjectById(projectId);

		switch (currentCrew.getRole()) {
			// SERVICE_ADMIN일 경우 통과
			case SERVICE_ADMIN:
				break;
			// COMPANY_ADMIN의 경우
			case COMPANY_ADMIN:
				// 해당 프로젝트에 참여하고 있는지 검증.
				Optional<Participant> participant1 = participantRepository.findByCrewIdAndProjectId(currentCrewId, projectId);

				// 해당 프로젝트에 참여중일 경우
				participant1.ifPresent(participant -> {
					// 프로젝트 권한 추출
					ProjectRole projectRoleOfParticipant1 = participant.getProjectRole();

					// 프로젝트 권한이 MANAGER도, ADMIN도 아닐 경우 throw FORBIDDEN.
					if (!projectRoleOfParticipant1.equals(ProjectRole.MANAGER) &&
						!projectRoleOfParticipant1.equals(ProjectRole.ADMIN)
					)
						throw new CustomCommonException(ErrorCode.FORBIDDEN);
				});

				// 해당 프로젝트에 참여중이 아닌경우
				if (participant1.isEmpty()) {
					// 현재 회원의 회사조회.
					Company companyOfCurrentProject = project.getCompany();

					// 본인의 회사와 프로젝트의 회사가 동일한지 검증. 아닐 경우 throw FORBIDDEN.
					if (!companyOfCurrentCrew.equals(companyOfCurrentProject))
						throw new CustomCommonException(ErrorCode.FORBIDDEN);
				}
				break;
			default:
				// 일반적인 경우
				// 현재 회원이 해당 프로젝트의 회원인지 검증, 조회.
				Participant participant = readParticipantByCrewIdAndProjectId(currentCrewId, projectId);

				// 참여자의 프로젝트 권한이 MANAGER인지 검증. 아닐 경우 throw FORBIDDEN.
				ProjectRole participantProjectRole = participant.getProjectRole();
				if (!participantProjectRole.equals(ProjectRole.MANAGER))
					throw new CustomCommonException(ErrorCode.FORBIDDEN);
		}

		// 프로젝트 정보 업데이트.
		project.update(projectReq);
	}

	@Transactional
	public void deleteProject(
		Long projectId,
		Long currentCrewId
	) {
		// 현재 회원 조회.
		Crew currentCrew = crewService.readCrewById(currentCrewId);
		// 현재 회원의 회사조회.
		Company companyOfCurrentCrew = currentCrew.getCompany();

		// 해당 프로젝트 조회.
		Project project = readProjectById(projectId);

		switch (currentCrew.getRole()) {
			// SERVICE_ADMIN일 경우 통과
			case SERVICE_ADMIN:
				break;
			// COMPANY_ADMIN의 경우
			case COMPANY_ADMIN:
				// 해당 프로젝트에 참여하고 있는지 검증.
				Optional<Participant> participant1 = participantRepository.findByCrewIdAndProjectId(currentCrewId, projectId);

				// 해당 프로젝트에 참여중일 경우
				participant1.ifPresent(participant -> {
					// 프로젝트 권한 추출
					ProjectRole projectRoleOfParticipant1 = participant.getProjectRole();

					// 프로젝트 권한이 ADMIN이 아닐 경우 throw FORBIDDEN.
					if (!projectRoleOfParticipant1.equals(ProjectRole.ADMIN))
						throw new CustomCommonException(ErrorCode.FORBIDDEN);
				});

				// 해당 프로젝트에 참여하지 않는 경우
				if (participant1.isEmpty()) {
					// 해당 프로젝트의 회사조회.
					Company companyOfCurrentProject = project.getCompany();

					// 현재 회원의 회사와 프로젝트의 회사정보가 일치하지 않을 경우 throw FORBIDDEN.
					if (!companyOfCurrentCrew.equals(companyOfCurrentProject))
						throw new CustomCommonException(ErrorCode.FORBIDDEN);
				}
				break;
			default:
				// 일반적인 경우 에러
				throw new CustomCommonException(ErrorCode.FORBIDDEN);
		}

		// 프로젝트 삭제
		projectRepository.deleteById(projectId);

		// 삭제 후 SERVICE_ADMIN에게 이메일 전송.
		emailUtil.sendProjectIsDeleted(currentCrew, project);
	}

	@Transactional(readOnly = true)
	public ProjectRes getProjectDtoByIdAndCrewId(
		Long currentCrewId,
		Long projectId
	) {
		// 현재 회원 조회
		Crew currentCrew = crewService.readCrewById(currentCrewId);
		// 현재 회원의 서비스 권한 조회
		Role roleOfCurrentCrew = currentCrew.getRole();
		// 현재 회원의 회사정보 조회
		Company companyOfCurrentCrew = currentCrew.getCompany();
		// 현재 프로젝트의 회사정보 조회
		Company companyOfCurrentProject = readProjectById(projectId).getCompany();

		// ADMIN이 아닌 경우
		if (!roleOfCurrentCrew.equals(Role.SERVICE_ADMIN) &&
			!roleOfCurrentCrew.equals(Role.COMPANY_ADMIN)) {
			// 현재 해당 프로젝트에 참여하는 인원인지 조회. 아니면 throw FORBIDDEN
			Participant participant = readParticipantByCrewIdAndProjectId(currentCrewId, projectId);

			// 참여자인데 PENDING이 아닌지 검증.
			if (participant.getProjectRole().equals(ProjectRole.PENDING))
				throw new CustomCommonException(ErrorCode.FORBIDDEN);
		}

		// COMPANY_ADMIN && 본인 회사의 프로젝트가 아닌 경우
		if (roleOfCurrentCrew.equals(Role.COMPANY_ADMIN) &&
			!companyOfCurrentCrew.equals(companyOfCurrentProject)) {
			// 다시 현재 해당 프로젝트에 참여하는 인원인지 조회. 아니면 throw FORBIDDEN
			readParticipantByCrewIdAndProjectId(currentCrewId, projectId);
		}

		// 프로젝트 조회.
		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new CustomCommonException(ErrorCode.PROJECT_NOT_FOUND));

		// 해당 프로젝트의 참여인원 전체조회.
		List<ParticipantDto> participantList = project.getParticipantList()
			.stream()
			.map(ParticipantDto::new)
			.collect(Collectors.toList());

		// 모든정보를 ProjectRes로 변환.
		return new ProjectRes(project, participantList, new CompanyDto(project.getCompany()));
	}

	@Transactional(readOnly = true)
	public Project readProjectById(Long projectId) {
		// ProjectId로 해당 프로젝트 조회.
		return projectRepository.findById(projectId)
			.orElseThrow(() -> new CustomCommonException(ErrorCode.PROJECT_NOT_FOUND));
	}

	@Transactional(readOnly = true)
	public void checkIsAllowedToRecord(Long crewId, Long projectId) {
		try {
//			Try: 현재 프로젝트에 참여하는 현재 회원에 한해서 검사.
			ProjectRole roleOfCurrentCrewInThisProject = readParticipantByCrewIdAndProjectId(crewId, projectId)
				.getProjectRole();

//			프로젝트 권한이 ADMIN, MANAGER, EDITOR중 하나라도 아닌 경우 Catch로 이동
			if ((!roleOfCurrentCrewInThisProject.equals(ProjectRole.ADMIN))
				&& (!roleOfCurrentCrewInThisProject.equals(ProjectRole.MANAGER))
				&& (!roleOfCurrentCrewInThisProject.equals(ProjectRole.EDITABLE)))
				throw new CustomCommonException(ErrorCode.FORBIDDEN);

		} catch (CustomCommonException e) {
			System.out.println("he's not allowed to edit");
//			현재 회원정보 조회.
			Crew currentCrew = crewService.readCrewById(crewId);
			System.out.println(currentCrew.getEmail());
//			SERVICE_ADMIN이 아닌경우
			if (!currentCrew.getRole().equals(Role.SERVICE_ADMIN)) {
				System.out.println("he's not a service_admin");
//				현재 프로젝트의 회사와 현재 회원의 회사가 일치하는지 조회
				Long companyIdOfCurrentProject = readProjectById(projectId)
					.getCompany()
					.getId();
				Long companyIdOfCurrentCrew = currentCrew
					.getCompany()
					.getId();
//				COMPANY_ADMIN가 아닌 경우
				if (!currentCrew.getRole().equals(Role.COMPANY_ADMIN)) {
					throw new CustomCommonException(ErrorCode.FORBIDDEN);
				} else {
//				COMPANY_ADMIN인 경우 프로젝트와 회사의 ID가 동일한지 조회
					if (!companyIdOfCurrentProject.equals(companyIdOfCurrentCrew)) {
						throw new CustomCommonException(ErrorCode.FORBIDDEN);
					}
				}
			}
		}
	}

	@Transactional(readOnly = true)
	public Participant readParticipantByCrewIdAndProjectId(
		Long currentCrewId,
		Long projectId
	) {
		// 현재 회원이 해당 프로젝트에 참여자 리스트에 존재하는지 조회.
		return participantRepository.findByCrewIdAndProjectId(currentCrewId, projectId)
			.orElseThrow(() -> new CustomCommonException(ErrorCode.PARTICIPANT_NOT_FOUND));
	}

	@Transactional
	public void addPanoramas(
		Long projectId,
		Long currentCrewId,
		PanoramaReq panoramaReq
	) {
		// 해당 프로젝트 조회.
		Project project = readProjectById(projectId);
		// 해당 프로젝트 회사정보 조회.
		Company prjectCompany = project.getCompany();

		// 현재 회원 조회
		Crew crew = crewService.readCrewById(currentCrewId);
		// 현재 회원의 회사정보 조회.
		Company crewCompany = crew.getCompany();

		// 현재 회원이 SERVICE_ADMIN이 아닌 경우. (COMPANY_ADMIN || ELSE)
		if (!crew.getRole().equals(Role.SERVICE_ADMIN))
			// 현재 회원의 회사와 해당 프로젝트의 회사가 동일한지 검증.
			if (!prjectCompany.equals(crewCompany))
				throw new CustomCommonException(ErrorCode.FORBIDDEN);

		// 프로젝트의 참여자 리스트 조회.
		List<Participant> participantList = project.getParticipantList();

		// 프로젝트의 참여자 리스트 중에서 본인이 있으면 조회. 없으면 isEmpty() == true.
		Optional<Participant> currentParticipant = participantList
			.stream()
			.filter(participant -> participant.getCrew().getId().equals(currentCrewId))
			.findAny();

		// 현재 회원이 이 프로젝트에 참여중인 경우.
		if (currentParticipant.isPresent()) {
			// (SERVICE_ADMIN || COMPANY_ADMIN)이 아니고,
			// 해당 프로젝트의 매니저도 아니라면 throw FORBIDDEN.
			if (
				!authService.isAdmin(currentCrewId) &&
					!currentParticipant.get().getProjectRole().equals(ProjectRole.MANAGER)
			)
				throw new CustomCommonException(ErrorCode.FORBIDDEN);

			// 현재 회원이 이 프로젝트에 참여중이지 않을 경우. throw FORBIDDEN.
		} else if (!authService.isAdmin(currentCrewId))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		// 여기까지 SERVICE_ADMIN,
		// (COMPANY_ADMIN's Company === Project's Company),
		// Project's MANAGER 일 경우 통과.

		// Request Body에서 base64 srcList 추출.
		List<String> srcList = panoramaReq.getPanoramaSrcList();

		// Panorama DB에서 해당 프로젝트에 등록된 파노라마가 이미 등록되어 있는지 확인.
		panoramaRepository
			.findByProjectId(projectId)
			.stream()
			.findAny()
			.ifPresent(panorama -> {
				// 이미 등록되어 있다면 모두 삭제.
				panoramaRepository.deleteAllByProjectId(projectId);
			});

		// Request Body에서 추출한 srcList -> Panorama Entity로 변형.
		List<Panorama> panoramaList = srcList
			.stream()
			.map(src -> new Panorama(src, project))
			.collect(Collectors.toList());

		// 파노라마 저장.
		panoramaRepository.saveAll(panoramaList);
	}

	public List<ParticipantDto> getParticipantListDto(List<Participant> participantList) {
		// List<Participant> -> List<ParticipantDto>
		return participantList
			.stream()
			.map(ParticipantDto::new)
			.collect(Collectors.toList());
	}
}
