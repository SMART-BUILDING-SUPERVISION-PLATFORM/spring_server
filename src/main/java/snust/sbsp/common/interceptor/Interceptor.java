package snust.sbsp.common.interceptor;

import lombok.RequiredArgsConstructor;
import org.springframework.web.servlet.HandlerInterceptor;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Arrays;

@RequiredArgsConstructor
public class Interceptor implements HandlerInterceptor {

	public static final String CURRENT_CREW_ID = "currentCrewId";

	@Override
	public boolean preHandle(
		HttpServletRequest request,
		HttpServletResponse response,
		Object handler
	) throws CustomCommonException {
		Cookie cookie;
		Cookie[] cookies = request.getCookies();

		try {
			if (cookies == null) throw new CustomCommonException(ErrorCode.SESSION_NOT_FOUND);

			cookie = Arrays.stream(cookies)
				.filter(c -> c.getName().equals("JSESSIONID"))
				.findAny()
				.orElseThrow(() -> new CustomCommonException(ErrorCode.SESSION_NOT_FOUND));
			
		} catch (NullPointerException e) {
			throw new CustomCommonException(ErrorCode.SESSION_NOT_FOUND);
		}

		String jSessionId = cookie.getValue();
		HttpSession session = request.getSession();
		Long currentCrewId = (Long) session.getAttribute(jSessionId);

		if (currentCrewId == null)
			throw new CustomCommonException(ErrorCode.SESSION_NOT_FOUND);

		request.setAttribute(CURRENT_CREW_ID, currentCrewId);

		return true;
	}
}
