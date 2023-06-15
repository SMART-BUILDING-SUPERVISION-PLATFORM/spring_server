package snust.sbsp.project.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum CtrType {

  CTR("건축공사"),
  CVL("토목공사"),
  PLT("플랜트공사"),
  ENV("조경공사");

  private final String value;

  public static CtrType from(String type) {
    return Arrays.stream(values())
      .filter(role -> role.value.equals(type))
      .findFirst()
      .orElseThrow(() -> new CustomCommonException(ErrorCode.BUSINESS_TYPE_INVALID));
  }
}
