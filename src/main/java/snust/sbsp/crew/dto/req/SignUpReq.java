package snust.sbsp.crew.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpReq {

  private Long companyId;

  private String email;

  private String password;

  private String name;

  private String number;

  private String businessType;

  private String validationCode;
}
