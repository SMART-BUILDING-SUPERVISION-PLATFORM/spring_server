package snust.sbsp.sign.dto.res;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignResDto {
  private Long id;
  private String message;

  @Builder
  public SignResDto(Long id, String message) {
    this.id = id;
    this.message = message;
  }
}
