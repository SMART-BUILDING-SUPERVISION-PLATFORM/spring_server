package snust.sbsp.crew.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CodeValidationReq {

  private final String email;

  private final String code;
}
