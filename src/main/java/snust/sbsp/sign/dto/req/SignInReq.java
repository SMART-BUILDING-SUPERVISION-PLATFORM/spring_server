package snust.sbsp.sign.dto.req;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SigninReqDto {
  private String email;
  private String password;

  @Builder
  public SigninReqDto(String email, String password) {
    this.email = email;
    this.password = password;
  }
}
