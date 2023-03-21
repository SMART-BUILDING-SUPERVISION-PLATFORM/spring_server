package snust.sbsp.crew.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignUpReq {

  private Long companyId;

  private String email;

  private String password;

  private String name;

  private String number;

  private String businessType;
}
