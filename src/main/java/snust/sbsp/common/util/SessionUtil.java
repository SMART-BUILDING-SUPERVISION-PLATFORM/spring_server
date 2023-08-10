package snust.sbsp.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.repository.CrewRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Component
@Transactional
@RequiredArgsConstructor
public class SessionUtil {

	private final CrewRepository crewRepository;

	public ResponseCookie createCookie(
		Crew crew,
		HttpServletRequest request
	) {
		// JSESSIONID 생성, Session Storage에 <JSESSIONID, CREW> 저장.
		String jSessionId = createSession(crew, request);

		// COOKIE 생성, 3시간짜리, 클라이언트 전송용.
		return ResponseCookie.from("JSESSIONID", jSessionId)
			.httpOnly(true)
			.secure(true)
			.path("/")
			.sameSite("none")
			.domain("localhost")
			.maxAge(60 * 60 * 3)
			.build();
	}

	public String createSession(
		Crew crew,
		HttpServletRequest request
	) {
		HttpSession session = request.getSession();
		String sessionId = session.getId();

		session.setAttribute(sessionId, crew.getId());

		return sessionId;
	}

	public void readSession(
		String jSessionId,
		HttpServletRequest request
	) {
		HttpSession session = request.getSession();
		
		if (session.getAttribute(jSessionId) != null)
			throw new CustomCommonException(ErrorCode.FORBIDDEN);
	}

	public void removeSession(
		String jSessionId,
		HttpServletRequest request
	) {
		// SESSION STORAGE에서 해당 CREW삭제.
		HttpSession session = request.getSession();
		session.removeAttribute(jSessionId);
	}
}
