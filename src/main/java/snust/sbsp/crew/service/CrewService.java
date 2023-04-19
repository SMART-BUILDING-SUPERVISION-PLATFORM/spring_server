package snust.sbsp.crew.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;
import snust.sbsp.common.util.SessionUtil;
import snust.sbsp.company.domain.Company;
import snust.sbsp.company.dto.res.base.CompanyDto;
import snust.sbsp.company.service.CompanyService;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.domain.type.Role;
import snust.sbsp.crew.dto.res.CrewRes;
import snust.sbsp.crew.repository.CrewRepository;
import snust.sbsp.crew.specification.CrewSpecification;
import snust.sbsp.project.service.ProjectService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CrewService {
  private final SessionUtil sessionUtil;

  private final CompanyService companyService;

  private final ProjectService projectService;

  private final CrewRepository crewRepository;

  @Transactional(readOnly = true)
  public Crew readCrew(String crewEmail) {

    return crewRepository.findByEmail(crewEmail)
      .orElseThrow(() -> new CustomCommonException(ErrorCode.CREW_NOT_FOUND));
  }

  @Transactional(readOnly = true)
  public CrewRes readCrew(Long crewId) {
    Crew crew = crewRepository.findById(crewId)
      .orElseThrow(() -> new CustomCommonException(ErrorCode.CREW_NOT_FOUND));

    return CrewRes.builder()
      .crew(crew)
      .company(new CompanyDto(crew.getCompany()))
      .projectList(projectService.readProjectList(crew))
      .build();
  }

  @Transactional(readOnly = true)
  public List<CrewRes> readCompanyCrewList(
    Long crewId,
    Boolean isPending,
    Role role,
    String name
  ) {
    Crew foundCrew = crewRepository.findById(crewId)
      .orElseThrow(() -> new CustomCommonException(ErrorCode.CREW_NOT_FOUND));

    if (foundCrew.getRole() != Role.COMPANY_ADMIN)
      throw new CustomCommonException(ErrorCode.FORBIDDEN);

    Specification<Crew> specification = ((root, query, criteriaBuilder) -> null);
    if (name != null)
      specification = specification.and(CrewSpecification.equalName(name));
    if (role != null)
      specification = specification.and(CrewSpecification.equalRole(role));
    if (isPending != null)
      specification = specification.and(CrewSpecification.equalIsPending(isPending));

    List<Crew> crewList = crewRepository.findAll(specification);

    return crewList
      .stream()
      .map(crew ->
        CrewRes
          .builder()
          .crew(crew)
          .company(new CompanyDto(crew.getCompany()))
          .build()
      ).collect(Collectors.toList());
  }

  public List<CrewRes> getAllCrewList(
    Long crewId,
    Long companyId,
    Boolean isPending,
    Role role,
    String name
  ) {
    Crew foundCrew = crewRepository.findById(crewId)
      .orElseThrow(() -> new CustomCommonException(ErrorCode.CREW_NOT_FOUND));

    if (foundCrew.getRole() != Role.COMPANY_ADMIN || foundCrew.getRole() != Role.SERVICE_ADMIN)
      throw new CustomCommonException(ErrorCode.FORBIDDEN);

    Specification<Crew> specification = ((root, query, criteriaBuilder) -> null);
    if (name != null)
      specification = specification.and(CrewSpecification.equalName(name));
    if (role != null)
      specification = specification.and(CrewSpecification.equalRole(role));
    if (isPending != null)
      specification = specification.and(CrewSpecification.equalIsPending(isPending));
    if (companyId != null) {
      Company company = companyService.findById(companyId);
      specification = specification.and(CrewSpecification.equalCompany(company));
    }

    List<Crew> crewList = crewRepository.findAll(specification);

    return crewList
      .stream()
      .map(crew ->
        CrewRes
          .builder()
          .crew(crew)
          .company(new CompanyDto(crew.getCompany()))
          .build()
      ).collect(Collectors.toList());
  }

  @Transactional
  public void togglePendingByCa(
    String jSessionId,
    HttpServletRequest request,
    Long crewId
  ) {
    Long requestedCrewId = sessionUtil.getInfo(jSessionId, request);
    Long requestedCompanyId = readCrew(requestedCrewId).getCompany().getId();
    Long crewCompanyId = readCrew(crewId).getCompany().getId();

    String isCaOrSa = isCaOrSa(requestedCrewId);

    if (!isCaOrSa.equals(Role.COMPANY_ADMIN.getValue()) || !requestedCompanyId.equals(crewCompanyId))
      throw new CustomCommonException(ErrorCode.FORBIDDEN);

    changeState(crewId);
  }

  @Transactional
  public void togglePendingBySa(
    String jSessionId,
    HttpServletRequest request,
    Long crewId
  ) {
    Long requestedCrewId = sessionUtil.getInfo(jSessionId, request);
    String isCaOrSa = isCaOrSa(requestedCrewId);

    if (!isCaOrSa.equals(Role.SERVICE_ADMIN.getValue()))
      throw new CustomCommonException(ErrorCode.FORBIDDEN);

    changeState(crewId);
  }

  private String isCaOrSa(
    Long crewId
  ) {
    Crew crew = crewRepository.findById(crewId)
      .orElseThrow(() -> new CustomCommonException(ErrorCode.CREW_NOT_FOUND));

    return crew.getRole().getValue();
  }

  private void changeState(Long crewId) {
    crewRepository.findById(crewId)
      .orElseThrow(() -> new CustomCommonException(ErrorCode.CREW_NOT_FOUND))
      .togglePending();
  }
}
