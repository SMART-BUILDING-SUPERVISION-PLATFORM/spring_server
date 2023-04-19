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
    String newCode = signupReq.getNewCode();

    if (!newCode.equals(redisUtil.getData(signupReq.getEmail())))
      throw new CustomCommonException(ErrorCode.FORBIDDEN);

    if (signupReq.getBusinessType().equals(Role.COMPANY_ADMIN.getValue()))
      isPossibleToJoin(signupReq);

    isEmailDuplicated(signupReq.getEmail());

    Crew crew = Crew.builder()
      .company(company)
      .email(signupReq.getEmail())
      .password(cryptoUtil.encrypt(signupReq.getPassword()))
      .name(signupReq.getName())
      .phone(signupReq.getNumber())
      .role(selectRole(signupReq.getBusinessType()))
      .isPending(true)
      .build();

    crewRepository.save(crew);

    redisUtil.deleteData(signupReq.getEmail());
  }

  @Transactional(readOnly = true)
  public Crew validateCrew(SignInReq signInReq) {
    Crew crew = crewService.readCrew(signInReq.getEmail());
    String decryptedPassword = cryptoUtil.decrypt(crew.getPassword());

    if (signInReq.getPassword().equals(decryptedPassword)) {
      if (crew.isPending())
        throw new CustomCommonException(ErrorCode.PENDING_STATE);
      return crew;
    } else
      throw new CustomCommonException(ErrorCode.PASSWORD_INVALID);
  }

  @Transactional(readOnly = true)
  public void isEmailDuplicated(String email) {
    if (crewRepository.findByEmail(email).isPresent())
      throw new CustomCommonException(ErrorCode.EMAIL_DUPLICATED);
  }

  private void isPossibleToJoin(SignUpReq signUpReq) {
    if (isAdminPresent(signUpReq))
      throw new CustomCommonException(ErrorCode.COMPANY_HAS_ADMIN);
  }

  @Transactional(readOnly = true)
  private boolean isAdminPresent(SignUpReq signUpReq) {
    Company company = companyService.findById(signUpReq.getCompanyId());

    return company.getCrewList()
      .stream()
      .anyMatch(crew -> crew.getRole().equals(Role.COMPANY_ADMIN));
  }

  private Role selectRole(String businessType) {
    Role crewRole;
    if (businessType.equals(Role.COMPANY_ADMIN.getValue()))
      crewRole = Role.COMPANY_ADMIN;
    else if (businessType.equals(Role.ORDER.getValue()))
      crewRole = Role.ORDER;
    else if (businessType.equals(Role.SUPERVISOR.getValue()))
      crewRole = Role.SUPERVISOR;
    else if (businessType.equals(Role.CONSTRUCTION.getValue()))
      crewRole = Role.CONSTRUCTION;
    else if (businessType.equals(Role.DESIGN.getValue()))
      crewRole = Role.DESIGN;
    else
      throw new CustomCommonException(ErrorCode.BUSINESS_TYPE_INVALID);

    return crewRole;
  }
}
