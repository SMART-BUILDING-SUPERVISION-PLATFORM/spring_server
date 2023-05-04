package snust.sbsp.crew.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;
import snust.sbsp.company.dto.res.base.CompanyDto;
import snust.sbsp.company.service.CompanyService;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.domain.type.Role;
import snust.sbsp.crew.dto.res.CrewRes;
import snust.sbsp.crew.repository.CrewRepository;
import snust.sbsp.crew.specification.CrewSpecification;
import snust.sbsp.project.dto.res.base.ProjectDto;
import snust.sbsp.project.service.ProjectService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CrewService {

    private final CompanyService companyService;

    private final CrewRepository crewRepository;

    private final CrewSpecification crewSpecification;

    @Transactional(readOnly = true)
    public Crew readCrewByEmail(String crewEmail) {
        return crewRepository.findByEmail(crewEmail)
                .orElseThrow(() -> new CustomCommonException(ErrorCode.CREW_NOT_FOUND));
    }

    public Crew readCrewById(Long crewId) {
        return crewRepository.findById(crewId)
                .orElseThrow(() -> new CustomCommonException(ErrorCode.CREW_NOT_FOUND));
    }

    public CrewRes readCrewInformation(Long crewId) {
        Crew crew = readCrewById(crewId);
        return CrewRes.builder()
                .crew(crew)
                .company(crew.getCompany().toDto())
                .projectList(crew.getProjectDtoList())
                .build();
    }

    @Transactional(readOnly = true)
    public List<CrewRes> readCompanyCrewList(
            Long crewId,
            Boolean isPending,
            Role role,
            String name
    ) {
        Crew foundCrew = readCrewById(crewId);

        if (!foundCrew.getRole().equals(Role.COMPANY_ADMIN))
            throw new CustomCommonException(ErrorCode.FORBIDDEN);

        Specification<Crew> specification = crewSpecification.getSpecification(name, role, isPending, null);
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

    @Transactional(readOnly = true)
    public List<CrewRes> getAllCrewList(
            Long crewId,
            Long companyId,
            Boolean isPending,
            Role role,
            String name
    ) {
        Crew foundCrew = readCrewById(crewId);

        if (!foundCrew.getRole().equals(Role.COMPANY_ADMIN) && !foundCrew.getRole().equals(Role.SERVICE_ADMIN))
            throw new CustomCommonException(ErrorCode.FORBIDDEN);

        Specification<Crew> specification = crewSpecification.getSpecification(name, role, isPending, companyId);

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
            Long companyAdminId,
            Long crewId
    ) {
        Crew companyAdmin = crewRepository.findByIdAndRole(companyAdminId, Role.COMPANY_ADMIN)
                .orElseThrow(() -> new CustomCommonException(ErrorCode.FORBIDDEN));

        Crew crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new CustomCommonException(ErrorCode.CREW_NOT_FOUND));

        if (!companyAdmin.getCompany().equals(crew.getCompany()))
            throw new CustomCommonException(ErrorCode.FORBIDDEN);

        crew.togglePending();
    }

    @Transactional
    public void togglePendingBySa(
            Long serviceAdminId,
            Long crewId
    ) {
        crewRepository.findByIdAndRole(serviceAdminId, Role.SERVICE_ADMIN)
                .orElseThrow(() -> new CustomCommonException(ErrorCode.FORBIDDEN));

        Crew crew = readCrewById(crewId);

        crew.togglePending();
    }

    @Transactional
    public void deleteCompanyCrew(Long companyAdminId, Long crewId) {
        Crew companyAdmin = crewRepository.findByIdAndRole(companyAdminId, Role.COMPANY_ADMIN)
                .orElseThrow(() -> new CustomCommonException(ErrorCode.FORBIDDEN));

        Crew crew = readCrewById(crewId);

        if (!companyAdmin.getCompany().equals(crew.getCompany()))
            throw new CustomCommonException(ErrorCode.FORBIDDEN);

        crewRepository.deleteById(crewId);
    }

    @Transactional
    public void deleteCrew(Long serviceAdminId, Long crewId) {
        crewRepository.findByIdAndRole(serviceAdminId, Role.SERVICE_ADMIN)
                .orElseThrow(() -> new CustomCommonException(ErrorCode.FORBIDDEN));

        readCrewById(crewId);

        crewRepository.deleteById(crewId);
    }
}
