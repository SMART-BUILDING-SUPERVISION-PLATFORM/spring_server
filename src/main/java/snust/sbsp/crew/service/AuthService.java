package snust.sbsp.crew.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;
import snust.sbsp.company.domain.Company;
import snust.sbsp.company.service.CompanyService;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.domain.type.Role;
import snust.sbsp.crew.dto.req.SignInReq;
import snust.sbsp.crew.dto.req.SignUpReq;
import snust.sbsp.crew.repository.CrewRepository;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final CrewRepository crewRepository;
    private final CompanyService companyService;
    private final CrewService crewService;

    @Value("${crypto.algorithm}")
    String alg;

    @Value("${crypto.aes-iv}")
    String aesIv;

    @Value("${crypto.aes-key}")
    String aesKey;

    @Transactional
    public Long join(SignUpReq signupReq) {
        Long companyId = signupReq.getCompanyId();
        Company company = companyService.findById(companyId);

        if (signupReq.getBusinessType().equals("관리자")) {
            isPossibleToJoin(signupReq);
        }

        isEmailDuplicated(signupReq.getEmail());

        Crew crew = Crew.builder()
                .company(company)
                .email(signupReq.getEmail())
                .password(encryptPassword(signupReq))
                .name(signupReq.getName())
                .phone(signupReq.getNumber())
                .businessType(signupReq.getBusinessType())
                .role(signupReq.getBusinessType().equals("관리자") ? Role.COMPANY_ADMIN : Role.USER)
                .isPending(true)
                .build();
        return crewRepository.save(crew).getId();
    }

    @Transactional(readOnly = true)
    public Crew validateCrew(SignInReq signInReq) {
        Crew crew = crewService.readCrew(signInReq.getEmail());
        String inComingCode = signInReq.getEmail() + signInReq.getPassword();

        String decryptedPassword = decryptPassword(crew.getPassword());
        if (inComingCode.equals(decryptedPassword)) {
            if (crew.isPending()) {
                throw new CustomCommonException(ErrorCode.PENDING_LOGIN);
            }
            return crew;
        } else {
            throw new CustomCommonException(ErrorCode.PASSWORD_INVALID);
        }
    }

    @Transactional(readOnly = true)
    public void isEmailDuplicated(String email) {
        if (crewRepository.findByEmail(email).isPresent()) {
            throw new CustomCommonException(ErrorCode.EMAIL_DUPLICATED);
        }
    }

    private void isPossibleToJoin(SignUpReq signUpReq) {
        if (isAdminPresent(signUpReq)) {
            throw new CustomCommonException(ErrorCode.COMPANY_HAS_ADMIN);
        }
    }

    private boolean isAdminPresent(SignUpReq signUpReq) {
        Long companyId = signUpReq.getCompanyId();
        Company company = companyService.findById(companyId);
        List<Crew> companyCrewList = company.getCrewList();
        Optional<Crew> companyAdmin = companyCrewList.stream().filter(crew ->
                crew.getRole().equals(Role.COMPANY_ADMIN)
        ).findAny();
        return companyAdmin.isPresent();

    }

    private String encryptPassword(SignUpReq signUpReq) {
        String encryptedCode = "";
        String target = signUpReq.getEmail() + signUpReq.getPassword();
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
