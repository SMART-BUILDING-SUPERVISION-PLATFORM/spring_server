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
import snust.sbsp.company.service.CompanyService;
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

	private final CompanyService companyService;

	private final CrewService crewService;

	private final EmailUtil emailUtil;

	private final AuthService authService;

	private final ProjectRepository projectRepository;

	private final PanoramaRepository panoramaRepository;

	private final ParticipantRepository participantRepository;

	@Transactional
	public void createProject(
		ProjectReq projectReq,
		Long currentCrewId
	) {
		Crew crew = crewService.readCrewById(currentCrewId);

		Role role = crew.getRole();
		if (!role.equals(Role.COMPANY_ADMIN) && !role.equals(Role.SERVICE_ADMIN))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

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

		List<ProjectRes> allProjectList = (List<ProjectRes>) projectRepository.findAll(specification)
			.stream()
			.map(project -> {
				return new ProjectRes(
					project,
					getParticipantListDto(project.getParticipantList()),
					new CompanyDto(project.getCompany()));
			}).collect(Collectors.toList());


		List<ProjectRes> myProjectList = readMyProjectList(
			currentCrewId,
			companyId,
			name,
			ctrType,
			detailCtrType
		);

		allProjectList.removeAll(myProjectList);

		List<ProjectRes> myPendingProjectList = readMyPendingProjectList(
			currentCrewId,
			companyId,
			name,
			ctrType,
			detailCtrType
		);

		allProjectList.removeAll(myPendingProjectList);

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
		Crew foundedCrew = crewService.readCrewById(currentCrewId);

		List<Participant> participantList = foundedCrew.getParticipantList();

		List<ProjectRes> allMyProjectList = convertToProjectRes(
			participantList,
			companyId,
			name,
			ctrType,
			detailCtrType
		);

		List<ProjectRes> myPendingProjectList = readMyPendingProjectList(
			currentCrewId,
			companyId,
			name,
			ctrType,
			detailCtrType);

		allMyProjectList.removeAll(myPendingProjectList);

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
		Crew foundedCrew = crewService.readCrewById(currentCrewId);

		List<Participant> participantList = foundedCrew.getParticipantList()
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
		return participantList
			.stream()
			.filter(
				participant -> {
					Project project = participant.getProject();

					boolean matchesCompanyId = companyId == null || companyId.equals(project.getCompany().getId());
					boolean matchesName = name == null || name.equals(project.getName());
					boolean matchesCtrType = ctrType == null || ctrType.equals(project.getCtrType().getValue());
					boolean matchesDetailCtrType = detailCtrType == null || detailCtrType.equals(project.getDetailCtrType().getValue());

					return matchesCompanyId && matchesName && matchesCtrType && matchesDetailCtrType;
				})
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
		Crew crew = crewService.readCrewById(currentCrewId);
		Project project = readProjectById(projectId);

		switch (crew.getRole()) {
			case SERVICE_ADMIN:
				break;
			case COMPANY_ADMIN:
				if (!project.getCompany().getId().equals(crew.getCompany().getId())) {
					throw new CustomCommonException(ErrorCode.FORBIDDEN);
				}
				break;
			default:
				Participant participant = readParticipantByCrewIdAndProjectId(currentCrewId, projectId);
				ProjectRole participantProjectRole = participant.getProjectRole();
				if (!participantProjectRole.equals(ProjectRole.MANAGER) && !participantProjectRole.equals(ProjectRole.EDITABLE)) {
					throw new CustomCommonException(ErrorCode.FORBIDDEN);
				}
		}

		project.update(projectReq);
	}

	@Transactional
	public void deleteProject(
		Long projectId,
		Long currentCrewId
	) {
		Crew crew = crewService.readCrewById(currentCrewId);
		Project project = readProjectById(projectId);

		switch (crew.getRole()) {
			case SERVICE_ADMIN:
				break;
			case COMPANY_ADMIN:
				if (!project.getCompany().getId().equals(crew.getCompany().getId()))
					throw new CustomCommonException(ErrorCode.FORBIDDEN);
				break;
			default:
				Participant participant = readParticipantByCrewIdAndProjectId(currentCrewId, projectId);
				ProjectRole participantProjectRole = participant.getProjectRole();
				if (!participantProjectRole.equals(ProjectRole.MANAGER))
					throw new CustomCommonException(ErrorCode.FORBIDDEN);
		}

		projectRepository.deleteById(projectId);
		emailUtil.sendProjectIsDeleted(crew, project);
	}

	@Transactional(readOnly = true)
	public ProjectRes getProjectDtoByIdAndCrewId(
		Long currentCrewId,
		Long projectId
	) {
		Crew currentCrew = crewService.readCrewById(currentCrewId);

		if (!currentCrew.getRole().equals(Role.SERVICE_ADMIN) && !currentCrew.getRole().equals(Role.COMPANY_ADMIN)) {
			Participant participant = readParticipantByCrewIdAndProjectId(currentCrewId, projectId);
			if (participant.getProjectRole().equals(ProjectRole.PENDING))
				throw new CustomCommonException(ErrorCode.FORBIDDEN);
		}

		Project project = projectRepository.findById(projectId)
			.orElseThrow(() -> new CustomCommonException(ErrorCode.PROJECT_NOT_FOUND));

		List<ParticipantDto> participantList = project.getParticipantList()
			.stream()
			.map(ParticipantDto::new)
			.collect(Collectors.toList());

		return new ProjectRes(project, participantList, new CompanyDto(project.getCompany()));
	}

	@Transactional(readOnly = true)
	public Project readProjectById(Long projectId) {
		return projectRepository.findById(projectId)
			.orElseThrow(() -> new CustomCommonException(ErrorCode.PROJECT_NOT_FOUND));
	}

	@Transactional(readOnly = true)
	public Participant readParticipantByCrewIdAndProjectId(
		Long currentCrewId,
		Long projectId
	) {
		return participantRepository.findByCrewIdAndProjectId(currentCrewId, projectId)
			.orElseThrow(() -> new CustomCommonException(ErrorCode.PARTICIPANT_NOT_FOUND));
	}

	@Transactional
	public void addPanoramas(
		Long projectId,
		Long currentCrewId,
		PanoramaReq panoramaReq
	) {
		Project project = readProjectById(projectId);
		Crew crew = crewService.readCrewById(currentCrewId);

		Company prjectCompany = project.getCompany();
		Company crewCompany = crew.getCompany();

		if (!crew.getRole().equals(Role.SERVICE_ADMIN)) {
			if (!prjectCompany.equals(crewCompany))
				throw new CustomCommonException(ErrorCode.FORBIDDEN);
		}

		List<Participant> participantList = project.getParticipantList();

		Optional<Participant> currentParticipant = participantList
			.stream()
			.filter(participant -> participant.getCrew().getId().equals(currentCrewId))
			.findAny();

		if (currentParticipant.isPresent()) {
			if (
				!authService.isAdmin(currentCrewId) &&
					!currentParticipant.get().getProjectRole().equals(ProjectRole.MANAGER)
			)
				throw new CustomCommonException(ErrorCode.FORBIDDEN);
		} else {
			if (!authService.isAdmin(currentCrewId))
				throw new CustomCommonException(ErrorCode.FORBIDDEN);
		}

		List<String> srcList = panoramaReq.getPanoramaSrcList();

		Optional<Panorama> panorama = panoramaRepository
			.findByProjectId(projectId)
			.stream()
			.findAny();

		if (panorama.isPresent())
			panoramaRepository.deleteAllByProjectId(projectId);

		List<Panorama> panoramaList = srcList
			.stream()
			.map(src -> new Panorama(src, project))
			.collect(Collectors.toList());

		panoramaRepository.saveAll(panoramaList);
	}

	public List<ParticipantDto> getParticipantListDto(List<Participant> participantList) {
		return participantList
			.stream()
			.map(ParticipantDto::new)
			.collect(Collectors.toList());
	}
}
