package snust.sbsp.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;
import snust.sbsp.company.domain.Company;
import snust.sbsp.company.repository.CompanyRepository;
import snust.sbsp.company.service.CompanyService;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.domain.type.Role;
import snust.sbsp.crew.service.CrewService;
import snust.sbsp.project.domain.Participant;
import snust.sbsp.project.domain.Project;
import snust.sbsp.project.domain.type.CtrType;
import snust.sbsp.project.domain.type.DetailCtrType;
import snust.sbsp.project.dto.req.ProjectReq;
import snust.sbsp.project.dto.res.base.ProjectDto;
import snust.sbsp.project.repository.ParticipantRepository;
import snust.sbsp.project.repository.ProjectRepository;
import snust.sbsp.project.specification.ProjectSpecification;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    private final ParticipantRepository participantRepository;

    private final CompanyService companyService;

    private final CrewService crewService;

    @Transactional
    public void createProject(
            ProjectReq projectReq,
            Long crewId
    ) {
        Crew crew = crewService.readCrewById(crewId);

        Role role = crew.getRole();
        if (!role.equals(Role.COMPANY_ADMIN) && !role.equals(Role.SERVICE_ADMIN))
            throw new CustomCommonException(ErrorCode.FORBIDDEN);

        Company company = role.equals(Role.COMPANY_ADMIN)
                ? crew.getCompany()
                : companyService.findById(projectReq.getCompanyId());

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
    public List<ProjectDto> readExceptMyProjectList(
            Long currentCrewId,
            Long companyId,
            String name,
            String ctrClass,
            String detailCtrClass
    ) {
        Specification<Project> specification = ((root, query, criteriaBuilder) -> null);
        if (name != null)
            specification = specification.and(ProjectSpecification.equalName(name));
        if (ctrClass != null)
            specification = specification.and(ProjectSpecification.equalCtrClass(CtrType.from(ctrClass)));
        if (detailCtrClass != null)
            specification = specification.and(ProjectSpecification.equalDetailCtrClass(DetailCtrType.from(detailCtrClass)));
        if (companyId != null) {
            specification = specification.and(ProjectSpecification.equalCompanyId(companyId));
        }

        List<ProjectDto> allProjectList = projectRepository.findAll(specification)
                .stream()
                .map((ProjectDto::new))
                .collect(Collectors.toList());

        List<ProjectDto> myProjectList = readMyProjectList(currentCrewId);

        allProjectList.removeAll(myProjectList);
        return allProjectList;
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> readMyProjectList(Long crewId) {
        Crew foundedCrew = crewService.readCrewById(crewId);

        return foundedCrew.getParticipantList()
                .stream()
                .map((participant -> new ProjectDto(participant.getProject())))
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateProject(
            Long projectId,
            ProjectReq projectReq,
            Long crewId
    ) {
        Crew crew = crewService.readCrewById(crewId);

        Role role = crew.getRole();
        if (!role.equals(Role.COMPANY_ADMIN) && !role.equals(Role.SERVICE_ADMIN))
            throw new CustomCommonException(ErrorCode.FORBIDDEN);

        Project project = readProjectById(projectId);

        Participant participant = readParticipantByCrewIdAndProjectIdAnd(crewId, projectId);
        snust.sbsp.project.domain.type.Role participantRole = participant.getRole();
        if (!participantRole.equals(snust.sbsp.project.domain.type.Role.MANAGER) && !participantRole.equals(snust.sbsp.project.domain.type.Role.EDITABLE)) {
            throw new CustomCommonException(ErrorCode.FORBIDDEN);
        }

        Company company = companyService.findById(projectReq.getCompanyId());

        project.update(company, projectReq);
    }

    @Transactional
    public void deleteProject(
            Long projectId,
            Long crewId
    ) {
        Crew crew = crewService.readCrewById(crewId);

        Role role = crew.getRole();
        if (!role.equals(Role.COMPANY_ADMIN) && !role.equals(Role.SERVICE_ADMIN))
            throw new CustomCommonException(ErrorCode.FORBIDDEN);

        readProjectById(projectId);

        Participant participant = readParticipantByCrewIdAndProjectIdAnd(crewId, projectId);
        snust.sbsp.project.domain.type.Role participantRole = participant.getRole();
        if (!participantRole.equals(snust.sbsp.project.domain.type.Role.MANAGER)) {
            throw new CustomCommonException(ErrorCode.FORBIDDEN);
        }

        projectRepository.deleteById(projectId);
    }

    @Transactional(readOnly = true)
    public Project readProjectById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new CustomCommonException(ErrorCode.PROJECT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Participant readParticipantByCrewIdAndProjectIdAnd(Long crewId, Long projectId) {
        return participantRepository.findByCrewIdAndProjectId(crewId, projectId)
                .orElseThrow(() -> new CustomCommonException(ErrorCode.PARTICIPANT_NOT_FOUND));
    }
}
