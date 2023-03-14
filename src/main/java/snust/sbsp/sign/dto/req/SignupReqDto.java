package snust.sbsp.sign.dto.req;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupReqDto {
  private Long companyId;
  private String email;
  private String password;
  private String name;
  private String number;
  private String businessType;

  @Builder
  public SignupReqDto(Long companyId, String email, String password, String name, String number, String businessType) {
    this.companyId = companyId;
    this.email = email;
    this.password = password;
    this.name = name;
    this.number = number;
    this.businessType = businessType;
  }
}
