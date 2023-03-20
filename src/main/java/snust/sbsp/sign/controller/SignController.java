package snust.sbsp.sign.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import snust.sbsp.common.res.Response;
import snust.sbsp.common.util.EmailUtil;
import snust.sbsp.common.util.SessionUtil;
import snust.sbsp.company.dto.res.CompanyRes;
import snust.sbsp.company.service.CompanyService;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.sign.dto.req.EmailValidationReq;
import snust.sbsp.sign.dto.req.SignInReq;
import snust.sbsp.sign.dto.req.SignUpReq;
import snust.sbsp.sign.service.SignService;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class SignController {

  private final EmailUtil emailUtil;

  private final SessionUtil sessionUtil;

  private final CompanyService companyService;

  private final SignService signService;


  @PostMapping("/sign-up")
  public ResponseEntity<?> join(@RequestBody SignUpReq signUpReq) {
    Long crewId = signService.join(signUpReq);
    if (crewId == null)
      return Response.fail(400, "company you've been selected has admin.");
    if (crewId < 0)
      return Response.fail(401, "email duplicated.");
    return Response.ok(201);
  }

  @PostMapping("/sign-in")
  public ResponseEntity<?> signIn(
    @RequestBody SignInReq signInReq,
    HttpServletRequest request
  ) {
    Optional<Crew> crew = signService.validateCrew(signInReq);
    if (crew.isEmpty()) {
      return Response.fail(404, "crew not found");
    }
    if (crew.get().isPending()) {
      return Response.fail(403, "you are in pending state.");
    }
    ResponseCookie responseCookie = sessionUtil.createCookie(crew.get(), request);
    return Response.ok(200, null, responseCookie);
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
      return Response.ok(200);
    }
    return Response.fail(401, "sign-out failed.");
  }

  @GetMapping("/company-list")
  public ResponseEntity<?> validateCompany(@PathParam("companyName") String companyName) {
    List<CompanyRes> companyList = companyService.findByName(companyName);
    if (companyList.size() == 0) {
      return Response.fail(404, "company not found");
    }
    return Response.ok(200, companyList);
  }

  @PostMapping("/email-duplication")
  public ResponseEntity<?> validateEmail(
    @RequestBody EmailValidationReq emailValidationReq) {
    String email = emailValidationReq.getEmail();
    if (signService.isEmailDuplicated(email)) {
      return Response.fail(401, "email duplicated");
    }
    try {
      emailUtil.sendSimpleMessage(email);
      return Response.ok(200);
    } catch (Exception e) {
      return Response.fail(500, e.getMessage());
    }
  }

  @PostMapping("/validate-code")
  public ResponseEntity<?> validateCode(
    @RequestBody EmailValidationReq emailValidationReq
  ) {
    String email = emailValidationReq.getEmail();
    String code = emailValidationReq.getCode();
    boolean isValidateCode = emailUtil.isValidateCode(email, code);
    if (isValidateCode)
      return Response.ok(200);
    return Response.fail(404, "code not found");
  }
}
