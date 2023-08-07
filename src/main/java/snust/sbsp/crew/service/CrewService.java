package snust.sbsp.crew.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;
import snust.sbsp.common.util.EmailUtil;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.domain.type.Role;
import snust.sbsp.crew.dto.res.CrewRes;
import snust.sbsp.crew.repository.CrewRepository;
import snust.sbsp.crew.specification.CrewSpecification;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CrewService {

	private final CrewRepository crewRepository;

	private final CrewSpecification crewSpecification;

	private final EmailUtil emailUtil;

	@Transactional(readOnly = true)
	public Crew readCrewByEmail(String crewEmail) {
		return crewRepository.findByEmail(crewEmail)
			.orElseThrow(() -> new CustomCommonException(ErrorCode.CREW_NOT_FOUND));
	}

	@Transactional(readOnly = true)
	public Crew readCrewById(Long crewId) {
		return crewRepository.findById(crewId)
			.orElseThrow(() -> new CustomCommonException(ErrorCode.CREW_NOT_FOUND));
	}

	@Transactional(readOnly = true)
	public Crew readCrewByIdAndRole(
		Long crewId,
		Role role
	) {
		System.out.println("crewId = " + crewId);
		System.out.println("role.getValue() = " + role.getValue());

		return crewRepository.findByIdAndRole(crewId, role)
			.orElseThrow(() -> new CustomCommonException(ErrorCode.FORBIDDEN));
	}

	@Transactional(readOnly = true)
	public CrewRes readCrewInformation(Long crewId) {
		Crew crew = readCrewById(crewId);
		return CrewRes.builder()
			.crew(crew)
			.company(crew.getCompany().toDto())
			.build();
	}

	@Transactional(readOnly = true)
	public List<CrewRes> readCompanyCrewList(
		Long currentCrewId,
		Boolean isPending,
		Role role,
		String name
	) {
		Crew currentCrew = readCrewByIdAndRole(currentCrewId, Role.COMPANY_ADMIN);
		Specification<Crew> specification = crewSpecification.getSpecification(name, role, isPending, currentCrew.getCompany().getId());

		return getCrewListExceptForMe(specification, currentCrewId)
			.stream()
			.map(crew ->
				CrewRes
					.builder()
					.crew(crew)
					.company(crew.getCompany().toDto())
					.build()
			).collect(Collectors.toList());
	}

	@Transactional(readOnly = true)
	public List<CrewRes> getAllCrewList(
		Long currentCrewId,
		Long companyId,
		Boolean isPending,
		Role role,
		String name
	) {
		Crew foundCrew = readCrewById(currentCrewId);

		if (!foundCrew.getRole().equals(Role.COMPANY_ADMIN) && !foundCrew.getRole().equals(Role.SERVICE_ADMIN))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		Specification<Crew> specification = crewSpecification.getSpecification(name, role, isPending, companyId);

		return getCrewListExceptForMe(specification, currentCrewId)
			.stream()
			.map(crew ->
				CrewRes
					.builder()
					.crew(crew)
					.company(crew.getCompany().toDto())
					.build()
			).collect(Collectors.toList());
	}

	@Transactional
	public void togglePendingByCa(
		Long companyAdminId,
		Long crewId
	) {
		Crew companyAdmin = readCrewByIdAndRole(companyAdminId, Role.COMPANY_ADMIN);

		Crew crew = readCrewById(crewId);

		if (!companyAdmin.getCompany().equals(crew.getCompany()))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		crew.togglePending();
		emailUtil.sendCrewIsBeingPending(crew.getEmail(), crew.getName(), crew.isPending());
	}

	@Transactional
	public void togglePendingBySa(
		Long serviceAdminId,
		Long crewId
	) {
		readCrewByIdAndRole(serviceAdminId, Role.SERVICE_ADMIN);

		Crew crew = readCrewById(crewId);

		crew.togglePending();
	}

	@Transactional
	public void deleteCompanyCrew(Long companyAdminId, Long crewId) {

		if (companyAdminId.equals(crewId))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		Crew companyAdmin = readCrewByIdAndRole(companyAdminId, Role.COMPANY_ADMIN);

		Crew crew = readCrewById(crewId);

		if (!companyAdmin.getCompany().equals(crew.getCompany()))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		crewRepository.deleteById(crewId);
		emailUtil.sendCrewIsDeleted(crew.getEmail(), crew.getName());
	}

	@Transactional
	public void deleteCrew(Long serviceAdminId, Long crewId) {
		if (serviceAdminId.equals(crewId))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		readCrewByIdAndRole(serviceAdminId, Role.SERVICE_ADMIN);

		Crew crew = readCrewById(crewId);

		crewRepository.deleteById(crewId);
		emailUtil.sendCrewIsDeleted(crew.getEmail(), crew.getName());
	}

	private List<Crew> getCrewListExceptForMe(
		Specification<Crew> specification,
		Long currentCrewId
	) {
		List<Crew> rawCrewList = crewRepository.findAll(specification);

		return rawCrewList.stream().filter(crew -> !crew.getId().equals(currentCrewId)).collect(Collectors.toList());
	}
}
