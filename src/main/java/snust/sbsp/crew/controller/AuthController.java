package snust.sbsp.crew.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import snust.sbsp.common.res.Response;
import snust.sbsp.common.util.EmailUtil;
import snust.sbsp.common.util.SessionUtil;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.dto.req.CodeValidationReq;
import snust.sbsp.crew.dto.req.EmailValidationReq;
import snust.sbsp.crew.dto.req.SignInReq;
import snust.sbsp.crew.dto.req.SignUpReq;
import snust.sbsp.crew.service.AuthService;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crew/auth")
public class AuthController {

  private final EmailUtil emailUtil;

  private final SessionUtil sessionUtil;

  private final AuthService authService;

  @PostMapping("/sign-up")
  public ResponseEntity<?> join(@RequestBody SignUpReq signUpReq) {
    authService.signUp(signUpReq);

    return Response.ok(HttpStatus.CREATED);
  }

  @PostMapping("/sign-in")
  public ResponseEntity<?> signIn(
    @RequestBody SignInReq signInReq,
    HttpServletRequest request
  ) {
    Crew crew = authService.validateCrew(signInReq);
    ResponseCookie responseCookie = sessionUtil.createCookie(crew, request);

    return Response.ok(HttpStatus.OK, null, responseCookie);
  }

  @GetMapping("/sign-out")
  public ResponseEntity<?> signOut(
    @CookieValue(
      value = "JSESSIONID"
    ) String jSessionId,
    HttpServletRequest request
  ) {
    sessionUtil.removeSession(jSessionId, request);

    return Response.ok(HttpStatus.OK);
  }

  @PostMapping("/email-duplication")
  public ResponseEntity<?> validateEmail(
    @RequestBody EmailValidationReq emailValidationReq
  ) {
    String email = emailValidationReq.getEmail();

    authService.isEmailDuplicated(email);
    emailUtil.sendSimpleMessage(email);

    return Response.ok(HttpStatus.OK);
  }

  @PostMapping("/validate-code")
  public ResponseEntity<?> validateCode(
    @RequestBody CodeValidationReq codeValidationReq
  ) {
    emailUtil.isValidateCode(codeValidationReq);

    return Response.ok(HttpStatus.OK);
  }
}
