package snust.sbsp.common.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;
import snust.sbsp.crew.dto.req.CodeValidationReq;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
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

  private MimeMessage createMessage(
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

  public void sendSimpleMessage(String to) {

    MimeMessage message;
    String code = createCode();

    try {
      message = createMessage(to, code);
      if (redisUtil.getData(to) != null)
        redisUtil.deleteData(to);

      redisUtil.setDataExpire(to, code, 3);
      javaMailSender.send(message);
    } catch (Exception e) {
      throw new CustomCommonException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
  }

  public void isValidateCode(CodeValidationReq codeValidationReq) {
    String email = codeValidationReq.getEmail();
    String code = codeValidationReq.getCode();

    String serverCode = redisUtil.getData(email);
    if (serverCode == null)
      throw new CustomCommonException(ErrorCode.EMAIL_CODE_NOT_FOUND);

    if (serverCode.equals(code))
      redisUtil.setDataExpire(email, "true", 10);
    else
      throw new CustomCommonException(ErrorCode.EMAIL_CODE_INVALID);
  }
}

