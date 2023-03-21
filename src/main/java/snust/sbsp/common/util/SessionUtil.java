package snust.sbsp.common.util;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import snust.sbsp.crew.domain.Crew;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

@Service
@Transactional
public class SessionUtil {

  public ResponseCookie createCookie(
    Crew crew,
    HttpServletRequest request
  ) {
    String jSessionId = createSession(crew, request);
    return ResponseCookie.from("JSESSIONID", jSessionId)
      .httpOnly(true)
      .secure(true)
      .path("/")
      .sameSite("none")
      .domain("localhost")
      .maxAge(60 * 60 * 3)
      .build();
  }

  public void removeSession(String jSessionId, HttpServletRequest request) {
    HttpSession session = request.getSession();
    session.removeAttribute(jSessionId);
    Long memberId = (Long) session.getAttribute(jSessionId);
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
}
