package snust.sbsp.project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;
import snust.sbsp.company.domain.Company;
import snust.sbsp.company.repository.CompanyRepository;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.domain.type.Role;
import snust.sbsp.crew.repository.CrewRepository;
import snust.sbsp.project.domain.Project;
import snust.sbsp.project.domain.type.CtrType;
import snust.sbsp.project.domain.type.DetailCtrType;
import snust.sbsp.project.dto.req.ProjectReq;
import snust.sbsp.project.dto.res.base.ProjectDto;
import snust.sbsp.project.repository.ProjectRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

  private final CompanyRepository companyRepository;

  private final ProjectRepository projectRepository;

  private final CrewRepository crewRepository;

  @Transactional
  public void createProject(
    ProjectReq projectReq,
    Long crewId
  ) {
    Crew crew = crewRepository.findById(crewId)
      .orElseThrow(() -> new CustomCommonException(ErrorCode.CREW_NOT_FOUND));

    Role role = crew.getRole();
    if (!role.equals(Role.COMPANY_ADMIN) && !role.equals(Role.SERVICE_ADMIN))
      throw new CustomCommonException(ErrorCode.FORBIDDEN);

    Company company = role.equals(Role.COMPANY_ADMIN)
      ? crew.getCompany()
      : companyRepository.findById(projectReq.getCompanyId())
      .orElseThrow(() -> new CustomCommonException(ErrorCode.COMPANY_NOT_FOUND));

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
  public List<ProjectDto> readProjectList(Crew crew) {
    
    return crew.getParticipantList()
      .stream()
      .map(participant -> new ProjectDto(participant.getProject()))
      .collect(Collectors.toList());
  }
}
