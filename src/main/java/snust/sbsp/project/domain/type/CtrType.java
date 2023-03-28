package snust.sbsp.project.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CtrType {
  CTR("건축공사"),
  CVL("토목공사"),
  PLT("플랜트공사"),
  ENV("조경공사");
  private final String value;
}
