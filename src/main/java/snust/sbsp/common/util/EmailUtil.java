package snust.sbsp.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.dto.req.CodeValidationReq;
import snust.sbsp.project.domain.Project;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailUtil {

	private final JavaMailSender javaMailSender;

	private final RedisUtil redisUtil;

	@Value("${spring.mail.username}")
	private String from;

	@Value("${spring.mail.host}")
	private String host;

	@Value("${spring.mail.port}")
	private String port;

	@Value("${spring.mail.password}")
	private String password;

	private String createCode() {
		Random random = new Random();
		StringBuilder key = new StringBuilder();

		for (int i = 0; i < 6; i++) {
			int index = random.nextInt(3);
			switch (index) {
				case 0:
					key.append((char) (random.nextInt(26) + 97));
					break;
				case 1:
					key.append((char) (random.nextInt(26) + 65));
					break;
				case 2:
					key.append(random.nextInt(9));
					break;
			}
		}

		return key.toString();
	}

	private MimeMessage createAuthMessage(
		String to,
		String code
	) throws MessagingException, UnsupportedEncodingException {
		MimeMessage message = javaMailSender.createMimeMessage();
		message.addRecipients(MimeMessage.RecipientType.TO, to);
		message.setSubject(to + "님, SMART BUILDING SUPERVISION PLATFORM 회원 인증코드입니다.");

		String msg = "";
		msg += "<h1 style=\"font-size: 30px; padding-right: 30px; padding-left: 30px;\">SMART BUILDING SUPERVISION PLATFORM 회원가입</h1>";
		msg += "<p style=\"font-size: 17px; padding-right: 30px; padding-left: 30px;\">아래 확인코드 6자리를 회원가입 화면에서 입력해주세요.</p>";
		msg += "<div style=\"padding-right: 30px; padding-left: 30px; margin: 32px 0 40px;\"><table style=\"border-collapse: collapse; border: 0; background-color: #F4F4F4; height: 70px; table-layout: fixed; word-wrap: break-word; border-radius: 6px;\"><tbody><tr><td style=\"text-align: center; vertical-align: middle; font-size: 30px;\">";
		msg += code;
		msg += "</td></tr></tbody></table></div>";

		message.setText(msg, "utf-8", "html");
		message.setFrom(new InternetAddress(from, "SMART BUILDING SUPERVISION PLATFORM"));

		return message;
	}

	private MimeMessage createCrewIsBeingPendingMessage(
		String to,
		String name,
		boolean isLocked
	) throws MessagingException, UnsupportedEncodingException {
		MimeMessage message = javaMailSender.createMimeMessage();
		message.addRecipients(MimeMessage.RecipientType.TO, to);
		message.setSubject("SMART BUILDING SUPERVISION PLATFORM 알림");

		String actionMessage = isLocked ? "계정이 잠금조치 되었음을 알려드립니다." : "계정인증이 완료되었음을 알려드립니다.";
		String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

		String msg = "";
		msg += "<p style=\"font-size: 17px; padding-right: 30px; padding-left: 30px;\">안녕하세요, " + name + "님.</p>";
		msg += "<p style=\"font-size: 17px; padding-right: 30px; padding-left: 30px;\">" + time + "부로</p>";
		msg += "<p style=\"font-size: 17px; padding-right: 30px; padding-left: 30px;\">" + actionMessage + "</p>";

		message.setText(msg, "utf-8", "html");
		message.setFrom(new InternetAddress(from, "SMART BUILDING SUPERVISION PLATFORM"));

		return message;
	}

	private MimeMessage createCrewDeletedMessage(
		String to,
		String name
	) throws MessagingException, UnsupportedEncodingException {
		MimeMessage message = javaMailSender.createMimeMessage();
		message.addRecipients(MimeMessage.RecipientType.TO, to);
		message.setSubject("SMART BUILDING SUPERVISION PLATFORM 알림");

		String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

		String msg = "";
		msg += "<p style=\"font-size: 17px; padding-right: 30px; padding-left: 30px;\">안녕하세요, " + name + "님.</p>";
		msg += "<p style=\"font-size: 17px; padding-right: 30px; padding-left: 30px;\">" + time + "부로</p>";
		msg += "<p style=\"font-size: 17px; padding-right: 30px; padding-left: 30px;\">귀하의 계정이 서비스로부터 삭제되었음을 알려드립니다.</p>";

		message.setText(msg, "utf-8", "html");
		message.setFrom(new InternetAddress(from, "SMART BUILDING SUPERVISION PLATFORM"));

		return message;
	}

	private MimeMessage createProjectIsDeletedMessage(
		String to,
		String deletedBy,
		String projectName,
		Long projectId,
		Long crewId
	) throws MessagingException, UnsupportedEncodingException {
		MimeMessage message = javaMailSender.createMimeMessage();
		message.addRecipients(MimeMessage.RecipientType.TO, to);
		message.setSubject("<!> SMART BUILDING SUPERVISION PLATFORM 알림 <!>");

		String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

		String msg = "";
		msg += "<p style=\"font-size: 17px; padding-right: 30px; padding-left: 30px;\">" + time + "부로</p>";
		msg += "<p style=\"font-size: 17px; padding-right: 30px; padding-left: 30px;\">" + "ProjectId: " + projectId + " : " + projectName + " 가" + "CrewId: " + crewId + " : " + deletedBy + "에 의하여 삭제되었음을 알려드립니다.</p>";

		message.setText(msg, "utf-8", "html");
		message.setFrom(new InternetAddress(from, "SMART BUILDING SUPERVISION PLATFORM"));

		return message;
	}

	public void sendProjectIsDeleted(
		Crew crew,
		Project project
	) {
		MimeMessage message;
		String to = crew.getEmail();
		String deletedBy = crew.getName();
		String projectName = project.getName();
		Long projectId = project.getId();
		Long crewId = crew.getId();

		try {
			message = createProjectIsDeletedMessage(to, deletedBy, projectName, projectId, crewId);
			javaMailSender.send(message);
			message = createProjectIsDeletedMessage(from, deletedBy, projectName, projectId, crewId);
			javaMailSender.send(message);
		} catch (Exception e) {
			throw new CustomCommonException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	public void sendCrewIsDeleted(
		String to,
		String name
	) {
		MimeMessage message;

		try {
			message = createCrewDeletedMessage(to, name);
			javaMailSender.send(message);
		} catch (Exception e) {
			throw new CustomCommonException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	public void sendCrewIsBeingPending(
		String to,
		String name,
		boolean isLocked
	) {
		MimeMessage message;

		try {
			message = createCrewIsBeingPendingMessage(to, name, isLocked);
			javaMailSender.send(message);
		} catch (Exception e) {
			throw new CustomCommonException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	public void sendCode(String to) {
		MimeMessage message;
		String code = createCode();

		try {
			message = createAuthMessage(to, code);
			if (redisUtil.getData(to) != null)
				redisUtil.deleteData(to);

			redisUtil.setDataExpire(to, code, 3);
			javaMailSender.send(message);
		} catch (Exception e) {
			throw new CustomCommonException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	public String isValidateCode(CodeValidationReq codeValidationReq) {
		String email = codeValidationReq.getEmail();
		String code = codeValidationReq.getCode();

		String serverCode = redisUtil.getData(email);

		if (serverCode == null)
			throw new CustomCommonException(ErrorCode.EMAIL_CODE_NOT_FOUND);

		if (serverCode.equals(code)) {
			String newCode = createCode();
			redisUtil.setDataExpire(email, newCode, 60);

			return redisUtil.getData(email);
		} else
			throw new CustomCommonException(ErrorCode.EMAIL_CODE_INVALID);
	}
}

