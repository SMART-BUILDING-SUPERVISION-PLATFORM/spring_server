package snust.sbsp.crew.dto.req;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class EmailValidationReq {

  private final String email;
  private final String name;
}
