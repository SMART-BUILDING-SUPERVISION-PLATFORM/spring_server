package snust.sbsp.crew.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;
import snust.sbsp.common.util.CryptoUtil;
import snust.sbsp.common.util.RedisUtil;
import snust.sbsp.company.domain.Company;
import snust.sbsp.company.service.CompanyService;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.domain.type.Role;
import snust.sbsp.crew.dto.req.SignInReq;
import snust.sbsp.crew.dto.req.SignUpReq;
import snust.sbsp.crew.repository.CrewRepository;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final CrewRepository crewRepository;

	private final CompanyService companyService;

	private final CrewService crewService;

	private final CryptoUtil cryptoUtil;

	private final RedisUtil redisUtil;

	@Transactional
	public void signUp(SignUpReq signupReq) {
		Long companyId = signupReq.getCompanyId();
		Company company = companyService.findById(companyId);
		String validationCode = signupReq.getValidationCode();

		// validation code 최종검증.
		if (!validationCode.equals(redisUtil.getData(signupReq.getEmail())))
			throw new CustomCommonException(ErrorCode.FORBIDDEN);

		// 선택 업종이 관리자인 경우 해당 회사에 관리자가 존재하는지 검증.
		if (signupReq.getBusinessType().equals(Role.COMPANY_ADMIN.getValue()))
			isPossibleToJoin(signupReq);

		// 이메일 중복 검증.
		isEmailDuplicated(signupReq.getEmail());

		// 최종 Entity build.
		Crew crew = Crew.builder()
			.company(company)
			.email(signupReq.getEmail())
			.password(cryptoUtil.encrypt(signupReq.getPassword()))
			.name(signupReq.getName())
			.phone(signupReq.getNumber())
			.role(Role.from(signupReq.getBusinessType()))
			.isPending(true)
			.build();

		crewRepository.save(crew);

		// 회원가입 완료시 redis에 저장된 개인정보 삭제.
		redisUtil.deleteData(signupReq.getEmail());
	}

	@Transactional(readOnly = true)
	public Crew validateCrew(SignInReq signInReq) {
		// 회원존재 검증 && 있으면 비밀번호 가져오기
		Crew crew = crewService.readCrewByEmail(signInReq.getEmail());
		String decryptedPassword = cryptoUtil.decrypt(crew.getPassword());

		// Input PW와 DB PW일치
		if (signInReq.getPassword().equals(decryptedPassword)) {

			// Role == PENDING이면 로그인 실패
			if (crew.isPending())
				throw new CustomCommonException(ErrorCode.PENDING_STATE);

			return crew;
		} else
			
			// PW 틀린 상황
			throw new CustomCommonException(ErrorCode.PASSWORD_INVALID);
	}

	@Transactional(readOnly = true)
	public void isEmailDuplicated(String email) {
		// 현재 Crew Table에 해당 Email을 가진 회원이 존재하는지 검사.
		if (crewRepository.findByEmail(email).isPresent())
			throw new CustomCommonException(ErrorCode.EMAIL_DUPLICATED);
	}

	@Transactional(readOnly = true)
	public boolean isAdminPresent(SignUpReq signUpReq) {
		Company company = companyService.findById(signUpReq.getCompanyId());

		return company.getCrewList()
			.stream()
			.anyMatch(crew -> crew.getRole().equals(Role.COMPANY_ADMIN));
	}

	@Transactional(readOnly = true)
	public void isPossibleToJoin(SignUpReq signUpReq) {
		if (isAdminPresent(signUpReq))
			throw new CustomCommonException(ErrorCode.COMPANY_HAS_ADMIN);
	}

	@Transactional(readOnly = true)
	public boolean isAdmin(Long crewId) {
		Role role = crewService.readCrewById(crewId).getRole();

		return role.equals(Role.SERVICE_ADMIN) || role.equals(Role.COMPANY_ADMIN);
	}
}
