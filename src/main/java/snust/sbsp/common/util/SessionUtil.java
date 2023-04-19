package snust.sbsp.common.util;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;
import snust.sbsp.crew.domain.Crew;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Component
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

  public String createSession(
    Crew crew,
    HttpServletRequest request
  ) {

    HttpSession session = request.getSession();
    String sessionId = session.getId();
    session.setAttribute(sessionId, crew.getId());
    return sessionId;
  }

  public void removeSession(
    String jSessionId,
    HttpServletRequest request
  ) {

    HttpSession session = request.getSession();
    session.removeAttribute(jSessionId);
  }

  public Long getInfo(
    String jSessionId,
    HttpServletRequest request
  ) {
    try {
      HttpSession session = request.getSession();

      return (Long) session.getAttribute(jSessionId);
    } catch (NullPointerException e) {
      throw new CustomCommonException(ErrorCode.SESSION_NOT_FOUND);
    }
  }
}
