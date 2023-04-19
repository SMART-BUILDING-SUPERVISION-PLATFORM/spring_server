package snust.sbsp.crew.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignUpReq {

  private final Long companyId;

  private final String email;

  private final String password;

  private final String name;

  private final String number;

  private final String businessType;

  private final String newCode;
}
