package snust.sbsp.crew.controller;

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
import snust.sbsp.crew.dto.req.EmailValidationReq;
import snust.sbsp.crew.dto.req.SignInReq;
import snust.sbsp.crew.dto.req.SignUpReq;
import snust.sbsp.crew.service.SignService;

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
    signService.join(signUpReq);

    return Response.ok(201);
  }

  @PostMapping("/sign-in")
  public ResponseEntity<?> signIn(
    @RequestBody SignInReq signInReq,
    HttpServletRequest request
  ) {
    Crew crew = signService.validateCrew(signInReq);

    ResponseCookie responseCookie = sessionUtil.createCookie(crew, request);
    return Response.ok(200, null, responseCookie);
  }

  @GetMapping("/sign-out")
  public ResponseEntity<?> signOut(
    @CookieValue(
      value = "JSESSIONID"
    ) String jSessionId,
    HttpServletRequest request
  ) {
    sessionUtil.removeSession(jSessionId, request);

    return Response.ok(200);
  }

  @GetMapping("/company-list")
  public ResponseEntity<?> validateCompany(@PathParam("companyName") String companyName) {
    List<CompanyRes> companyList = companyService.findByName(companyName);

    return Response.ok(200, companyList);
  }

  @PostMapping("/email-duplication")
  public ResponseEntity<?> validateEmail(
    @RequestBody EmailValidationReq emailValidationReq) {
    String email = emailValidationReq.getEmail();
    signService.isEmailDuplicated(email);
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
    emailUtil.isValidateCode(emailValidationReq);

    return Response.ok(200);
  }
}
