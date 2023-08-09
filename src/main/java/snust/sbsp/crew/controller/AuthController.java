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
import snust.sbsp.crew.dto.req.SignInReq;
import snust.sbsp.crew.dto.req.SignUpReq;
import snust.sbsp.crew.dto.res.base.CrewDto;
import snust.sbsp.crew.dto.res.etc.ValidationCodeDto;
import snust.sbsp.crew.service.AuthService;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crew/auth")
public class AuthController {

	private final EmailUtil emailUtil;

	private final SessionUtil sessionUtil;

	private final AuthService authService;

	// test complete
	@GetMapping("/check")
	public ResponseEntity<CrewDto> checkLoggedIn(
		@CookieValue(
			value = "JSESSIONID"
		) String jSessionId,
		HttpServletRequest request
	) {
		sessionUtil.readSession(jSessionId, request);
//		HttpSession session = request.getSession();
//		Crew crew = (Crew) session.getAttribute(jSessionId);

		return Response.ok(HttpStatus.OK);
	}

	// test complete
	@PostMapping("/sign-up")
	public ResponseEntity<?> join(
		@RequestBody SignUpReq signUpReq
	) {
		authService.signUp(signUpReq);

		return Response.ok(HttpStatus.CREATED);
	}

	// test complete
	@PostMapping("/sign-in")
	public ResponseEntity<?> signIn(
		@RequestBody SignInReq signInReq,
		HttpServletRequest request
	) {
		Crew crew = authService.validateCrew(signInReq);
		ResponseCookie responseCookie = sessionUtil.createCookie(crew, request);

		return Response.ok(HttpStatus.OK, null, responseCookie);
	}

	// test complete
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

	// test complete
	@GetMapping("/email-duplication")
	public ResponseEntity<?> validateEmail(
		@PathParam("email") String email
	) {
		authService.isEmailDuplicated(email);
		emailUtil.sendCode(email);

		return Response.ok(HttpStatus.OK);
	}

	// test complete
	@PostMapping("/validate-code")
	public ResponseEntity<ValidationCodeDto> validateCode(
		@RequestBody CodeValidationReq codeValidationReq
	) {
		String validateCode = emailUtil.isValidateCode(codeValidationReq);

		return Response.ok(HttpStatus.OK, new ValidationCodeDto(validateCode));
	}
}
