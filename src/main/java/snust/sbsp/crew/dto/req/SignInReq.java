package snust.sbsp.crew.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignInReq {

  private String email;

  private String password;
}
