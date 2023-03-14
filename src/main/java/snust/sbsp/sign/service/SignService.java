package snust.sbsp.sign.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import snust.sbsp.company.domain.Company;
import snust.sbsp.company.repository.CompanyRepository;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.domain.type.Role;
import snust.sbsp.crew.repository.CrewRepository;
import snust.sbsp.sign.dto.req.SigninReqDto;
import snust.sbsp.sign.dto.req.SignupReqDto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.transaction.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SignService {
  private final CrewRepository crewRepository;
  private final CompanyRepository companyRepository;
  @Value("${crypto.algorithm}")
  String alg;
  @Value("${crypto.aes-iv}")
  String aesIv;
  @Value("${crypto.aes-key}")
  String aesKey;

  @Autowired
  public SignService(
    CrewRepository crewRepository,
    CompanyRepository companyRepository
  ) {
    this.crewRepository = crewRepository;
    this.companyRepository = companyRepository;
  }

  public Long join(SignupReqDto signupReqDto) {
    Long companyId = signupReqDto.getCompanyId();
    Optional<Company> company = companyRepository.findById(companyId);
    if (company.isEmpty()) {
      return null;
    }
    if (!isPossibleToJoin(signupReqDto)) {
      return null;
    }
    Crew crew = Crew.builder()
      .company(company.get())
      .email(signupReqDto.getEmail())
      .password(encryptPassword(signupReqDto))
      .name(signupReqDto.getName())
      .phone(signupReqDto.getNumber())
      .businessType(signupReqDto.getBusinessType())
      .role(signupReqDto.getBusinessType().equals("관리자") ? Role.COMPANY_ADMIN : Role.PENDING)
      .build();
    return crewRepository.save(crew).getId();
  }

  public Optional<Crew> validateCrew(SigninReqDto signInReqDto) {
    Optional<Crew> member = crewRepository.findByEmail(signInReqDto.getEmail());
    if (member.isPresent()) {
      String inComingCode = signInReqDto.getEmail() + signInReqDto.getPassword();
      String decryptedPassword = decryptPassword(member.get().getPassword());
      if (inComingCode.equals(decryptedPassword)) {
        return member;
      } else {
        return Optional.empty();
      }
    } else {
      return Optional.empty();
    }
  }

  public Optional<Crew> isEmailDuplicated(String email) {
    return crewRepository.findByEmail(email);
  }

  private Optional<Company> getCompany(SignupReqDto signUpReqDto) {
    Long companyId = signUpReqDto.getCompanyId();
    return companyRepository.findById(companyId);
  }

  private boolean isPossibleToJoin(SignupReqDto signUpReqDto) {
    String businessType = signUpReqDto.getBusinessType();
    boolean isAdminPresent = isAdminPresent(signUpReqDto);
    if (businessType.equals("관리자")) {
      return !isAdminPresent;
    } else {
      return isAdminPresent;
    }
  }

  private boolean isAdminPresent(SignupReqDto signUpReqDto) {
    Long companyId = signUpReqDto.getCompanyId();
    Optional<Company> company = companyRepository.findById(companyId);
    if (company.isPresent()) {
      List<Crew> companyCrewList = company.get().getCrewList();
      Optional<Crew> companyAdmin = companyCrewList.stream().filter(crew ->
        crew.getRole().equals(Role.COMPANY_ADMIN)
      ).findAny();
      return companyAdmin.isPresent();
    }
    return false;
  }

  private String encryptPassword(SignupReqDto signUpReqDto) {
    String encryptedCode = "";
    String target = signUpReqDto.getEmail() + signUpReqDto.getPassword();
    try {
      Cipher cipher = Cipher.getInstance(alg);
      SecretKeySpec keySpec = new SecretKeySpec(aesKey.getBytes(), "AES");
      IvParameterSpec ivParameterSpec = new IvParameterSpec(aesIv.getBytes());
      cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivParameterSpec);
      byte[] encrypted1 = cipher.doFinal(target.getBytes(StandardCharsets.UTF_8));
      encryptedCode = Base64.getEncoder().encodeToString(encrypted1);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return encryptedCode;
  }

  private String decryptPassword(String encryptedCode) {
    String decryptedPassword = "";
    try {
      Cipher cipher = Cipher.getInstance(alg);
      SecretKeySpec keySpec = new SecretKeySpec(aesKey.getBytes(), "AES");
      IvParameterSpec ivParameterSpec = new IvParameterSpec(aesIv.getBytes());
      cipher.init(Cipher.DECRYPT_MODE, keySpec, ivParameterSpec);
      byte[] decodedBytes = Base64.getDecoder().decode(encryptedCode);
      byte[] decrypted = cipher.doFinal(decodedBytes);
      decryptedPassword = new String(decrypted);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return decryptedPassword;
  }
}
