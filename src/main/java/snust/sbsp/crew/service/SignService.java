package snust.sbsp.crew.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import snust.sbsp.company.domain.Company;
import snust.sbsp.company.repository.CompanyRepository;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.domain.type.Role;
import snust.sbsp.crew.dto.req.SignInReq;
import snust.sbsp.crew.dto.req.SignUpReq;
import snust.sbsp.crew.repository.CrewRepository;

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
@RequiredArgsConstructor
public class SignService {

    private final CrewRepository crewRepository;

    private final CompanyRepository companyRepository;

    @Value("${crypto.algorithm}")
    String alg;

    @Value("${crypto.aes-iv}")
    String aesIv;

    @Value("${crypto.aes-key}")
    String aesKey;

    public Long join(SignUpReq signupReq) {
        Long companyId = signupReq.getCompanyId();
        Optional<Company> company = companyRepository.findById(companyId);
        if (company.isEmpty()) {
            return null;
        }
        if (!isPossibleToJoin(signupReq)) {
            return null;
        }
        if (isEmailDuplicated(signupReq.getEmail())) {
            return -1L;
        }
        Crew crew = Crew.builder()
                .company(company.get())
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

    public Optional<Crew> validateCrew(SignInReq signInReq) {
        Optional<Crew> member = crewRepository.findByEmail(signInReq.getEmail());
        if (member.isPresent()) {
            String inComingCode = signInReq.getEmail() + signInReq.getPassword();
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

    public boolean isEmailDuplicated(String email) {
        return crewRepository.findByEmail(email).isPresent();
    }

    private boolean isPossibleToJoin(SignUpReq signUpReq) {
        String businessType = signUpReq.getBusinessType();
        boolean isAdminPresent = isAdminPresent(signUpReq);
        if (businessType.equals("관리자") && isAdminPresent) {
            return false;
        } else if (businessType.equals("관리자")) {
            return true;
        } else {
            return true;
        }
    }

    private boolean isAdminPresent(SignUpReq signUpReq) {
        Long companyId = signUpReq.getCompanyId();
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
