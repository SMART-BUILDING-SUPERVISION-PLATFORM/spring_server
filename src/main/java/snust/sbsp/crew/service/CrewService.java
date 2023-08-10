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
		// 이메일로 회원조회.
		return crewRepository.findByEmail(crewEmail)
			.orElseThrow(() -> new CustomCommonException(ErrorCode.CREW_NOT_FOUND));
	}

	@Transactional(readOnly = true)
	public Crew readCrewById(Long crewId) {
		// 해당 CrewID에 매칭되는 회원조회.
		return crewRepository.findById(crewId)
			.orElseThrow(() -> new CustomCommonException(ErrorCode.CREW_NOT_FOUND));
	}

	@Transactional(readOnly = true)
	public Crew readCrewByIdAndRole(
		Long crewId,
		Role role
	) {
		// CrewID 와 Role을 동시에 만족하는 회원 반환. 없으면 FORBIDDEN
		return crewRepository.findByIdAndRole(crewId, role)
			.orElseThrow(() -> new CustomCommonException(ErrorCode.FORBIDDEN));
	}

	@Transactional(readOnly = true)
	public CrewRes readCrewInformation(Long crewId) {
		// CrewID로 회원정보 조회
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
		// 본인이 존재하고, 동시에 회사 관리자인지 검증.
		Crew currentCrew = readCrewByIdAndRole(currentCrewId, Role.COMPANY_ADMIN);

		// DB조회 다중쿼리 생성
		Specification<Crew> specification = crewSpecification.getSpecification(name, role, isPending, currentCrew.getCompany().getId());

		// 회원리스트 DTO로 변환 후 반환
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
		// 현재 로그인 되어있는 회원 조회
		Crew currentCrew = readCrewById(currentCrewId);

		// 현재 회원이 COMPANY_ADMIN 도 아니고 SERVICE_ADMIN도 아니라면 FORBIDDEN.
		if (!currentCrew.getRole().equals(Role.COMPANY_ADMIN) && !currentCrew.getRole().equals(Role.SERVICE_ADMIN))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		// 다중쿼리 생성
		Specification<Crew> specification = crewSpecification.getSpecification(name, role, isPending, companyId);

		// 회원리스트 DTO로 변환 후 반환
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
		Long currentCrewId,
		Long crewId
	) {
		// 본인이 존재하고, 동시에 회사 관리자인지 검증.
		Crew currentCrew = readCrewByIdAndRole(currentCrewId, Role.COMPANY_ADMIN);

		// 권한 변경대상 회원 조회
		Crew crew = readCrewById(crewId);

		// 현재 회원과 대상 회원의 회사가 일치하는지 검증
		if (!currentCrew.getCompany().equals(crew.getCompany()))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		// 대상회원 권한 변경.
		crew.togglePending();

		// 해당 회원 이메일로 권한변경 이메일 전송
		emailUtil.sendCrewIsBeingPending(crew.getEmail(), crew.getName(), crew.isPending());
	}

	@Transactional
	public void togglePendingBySa(
		Long currentCrewId,
		Long crewId
	) {
		// 현재 회원이 SERVICE_ADMIN인지 검증.
		readCrewByIdAndRole(currentCrewId, Role.SERVICE_ADMIN);

		// 권한 변경 대상회원 조회
		Crew crew = readCrewById(crewId);

		// 회원 권한 변경
		crew.togglePending();
	}

	@Transactional
	public void deleteByCa(
		Long currentCrewId,
		Long crewId
	) {
		// 현재 회원과 삭제 대상 회원의 CrewID가 같지 않은지 검증.
		if (currentCrewId.equals(crewId))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		// 현재 회원이 COMPANY_ADMIN인지 검증.
		Crew companyAdmin = readCrewByIdAndRole(currentCrewId, Role.COMPANY_ADMIN);

		// 삭제 대상 회원 조회.
		Crew crew = readCrewById(crewId);

		// 현재 회원의 회사와 대상 회원의 회사가 같지 않은지 검증.
		if (!companyAdmin.getCompany().equals(crew.getCompany()))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		// 대상회원 삭제.
		crewRepository.deleteById(crewId);

		// 삭제된 회원 이메일로 이메일 전송.
		emailUtil.sendCrewIsDeleted(crew.getEmail(), crew.getName());
	}

	@Transactional
	public void deleteBySa(Long serviceAdminId, Long crewId) {
		// 현재 회원과 삭제 대상 회원의 CrewID가 같지 않은지 검증.
		if (serviceAdminId.equals(crewId))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		// 현재 회원이 SERVICE_ADMIN인지 검증.
		readCrewByIdAndRole(serviceAdminId, Role.SERVICE_ADMIN);

		// 삭제 대상 회원 조회.
		Crew crew = readCrewById(crewId);

		// 대상회원 삭제.
		crewRepository.deleteById(crewId);

		// 삭제된 회원 이메일로 이메일 전송.
		emailUtil.sendCrewIsDeleted(crew.getEmail(), crew.getName());
	}

	private List<Crew> getCrewListExceptForMe(
		Specification<Crew> specification,
		Long currentCrewId
	) {
		// 다중쿼리 적용하여 회원리스트 조회
		List<Crew> rawCrewList = crewRepository.findAll(specification);

		// 본인(관리자)제외한 회원리스트 반환
		return rawCrewList
			.stream()
			.filter(
				crew ->
					!crew.getId().equals(currentCrewId)).collect(Collectors.toList()
			);
	}
}
