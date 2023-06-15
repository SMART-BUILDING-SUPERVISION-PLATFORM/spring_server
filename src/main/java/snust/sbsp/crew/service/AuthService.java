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
    String newCode = signupReq.getValidationCode();

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
      .role(Role.from(signupReq.getBusinessType()))
      .isPending(true)
      .build();

    crewRepository.save(crew);

    redisUtil.deleteData(signupReq.getEmail());
  }

  @Transactional(readOnly = true)
  public Crew validateCrew(SignInReq signInReq) {
    Crew crew = crewService.readCrewByEmail(signInReq.getEmail());
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

  @Transactional(readOnly = true)
  private boolean isAdminPresent(SignUpReq signUpReq) {
    Company company = companyService.findById(signUpReq.getCompanyId());

    return company.getCrewList()
      .stream()
      .anyMatch(crew -> crew.getRole().equals(Role.COMPANY_ADMIN));
  }

  private void isPossibleToJoin(SignUpReq signUpReq) {
    if (isAdminPresent(signUpReq))
      throw new CustomCommonException(ErrorCode.COMPANY_HAS_ADMIN);
  }

}
