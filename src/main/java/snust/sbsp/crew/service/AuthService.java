package snust.sbsp.crew.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;
import snust.sbsp.common.util.CryptoUtil;
import snust.sbsp.company.domain.Company;
import snust.sbsp.company.service.CompanyService;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.domain.type.Role;
import snust.sbsp.crew.dto.req.SignInReq;
import snust.sbsp.crew.dto.req.SignUpReq;
import snust.sbsp.crew.repository.CrewRepository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final CrewRepository crewRepository;
    private final CompanyService companyService;
    private final CrewService crewService;
    private final CryptoUtil cryptoUtil;

    @Transactional
    public void signUp(SignUpReq signupReq) {
        Long companyId = signupReq.getCompanyId();
        Company company = companyService.findById(companyId);

        if (signupReq.getBusinessType().equals("관리자")) {
            isPossibleToJoin(signupReq);
        }

        isEmailDuplicated(signupReq.getEmail());

        Crew crew = Crew.builder()
                .company(company)
                .email(signupReq.getEmail())
                .password(cryptoUtil.encrypt(signupReq.getPassword()))
                .name(signupReq.getName())
                .phone(signupReq.getNumber())
                .businessType(signupReq.getBusinessType())
                .role(signupReq.getBusinessType().equals("관리자") ? Role.COMPANY_ADMIN : Role.USER)
                .isPending(true)
                .build();
        crewRepository.save(crew);
    }

    @Transactional(readOnly = true)
    public Crew validateCrew(SignInReq signInReq) {
        Crew crew = crewService.readCrew(signInReq.getEmail());
        String decryptedPassword = cryptoUtil.decrypt(crew.getPassword());

        if (signInReq.getPassword().equals(decryptedPassword)) {
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

}
