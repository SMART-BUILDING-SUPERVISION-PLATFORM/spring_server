package snust.sbsp.sign.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import snust.sbsp.common.dto.ResponseDto;
import snust.sbsp.common.util.EmailUtil;
import snust.sbsp.common.util.SessionUtil;
import snust.sbsp.company.dto.res.CompanyResDto;
import snust.sbsp.company.service.CompanyService;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.domain.type.Role;
import snust.sbsp.sign.dto.req.EmailValidationReqDto;
import snust.sbsp.sign.dto.req.SigninReqDto;
import snust.sbsp.sign.dto.req.SignupReqDto;
import snust.sbsp.sign.service.SignService;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/auth")
public class SignController {
  private final EmailUtil emailUtil;
  private final SessionUtil sessionUtil;
  private final CompanyService companyService;
  private final SignService signService;

  @Autowired
  public SignController(
    EmailUtil emailUtil,
    SessionUtil sessionUtil,
    CompanyService companyService,
    SignService signService
  ) {
    this.emailUtil = emailUtil;
    this.sessionUtil = sessionUtil;
    this.companyService = companyService;
    this.signService = signService;
  }


  @PostMapping("/sign-up")
  public ResponseEntity<?> join(@RequestBody SignupReqDto signupReqDto) {
    Long crewId = signService.join(signupReqDto);
    if (crewId == null) {
      return ResponseDto.fail(400, "company you've been selected has admin.");
    }
    return ResponseDto.ok(201);
  }

  @PostMapping("/sign-in")
  public ResponseEntity<?> signIn(
    @RequestBody SigninReqDto signInReqDto,
    HttpServletRequest request
  ) {
    Optional<Crew> crew = signService.validateCrew(signInReqDto);
    if (crew.isEmpty()) {
      return ResponseDto.fail(404, "crew not found");
    }
    if (crew.get().getRole().equals(Role.PENDING)) {
      return ResponseDto.fail(403, "unauthorized crew.");
    }
    ResponseCookie responseCookie = sessionUtil.createCookie(crew.get(), request);
    return ResponseDto.ok(200, null, responseCookie);
  }

  @GetMapping("/sign-out")
  public ResponseEntity<?> signOut(
    @CookieValue(
      value = "JSESSIONID"
    ) String jSessionId,
    HttpServletRequest request
  ) {
    boolean isSessionRemoved = sessionUtil.removeSession(jSessionId, request);
    if (isSessionRemoved) {
      return ResponseDto.ok(200);
    }
    return ResponseDto.fail(401, "sign-out failed.");
  }

  @GetMapping("/company-list")
  public ResponseEntity<?> validateCompany(@PathParam("companyName") String companyName) {
    List<CompanyResDto> companyList = companyService.findByName(companyName);
    if (companyList.size() == 0) {
      return ResponseDto.fail(404, "company not found");
    }
    return ResponseDto.ok(200, companyList);
  }

  @PostMapping("/email-duplication")
  public ResponseEntity<?> validateEmail(
    @RequestBody EmailValidationReqDto emailValidationReqDto) {
    String email = emailValidationReqDto.getEmail();
    Optional<Crew> member = signService.isEmailDuplicated(email);
    if (member.isPresent()) {
      return ResponseDto.fail(401, "email duplicated");
    }
    try {
      emailUtil.sendSimpleMessage(email);
      return ResponseDto.ok(200);
    } catch (Exception e) {
      return ResponseDto.fail(500, e.getMessage());
    }
  }

  @PostMapping("/validate-code")
  public ResponseEntity<?> validateCode(
    @RequestBody EmailValidationReqDto emailValidationReqDto
  ) {
    String email = emailValidationReqDto.getEmail();
    String code = emailValidationReqDto.getCode();
    boolean isValidateCode = emailUtil.isValidateCode(email, code);
    if (isValidateCode)
      return ResponseDto.ok(200);
    return ResponseDto.fail(404, "code not found");
  }
}
