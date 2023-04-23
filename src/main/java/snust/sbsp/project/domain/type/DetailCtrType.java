package snust.sbsp.project.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum DetailCtrType {

  LIV("주거용 건축물"),
  OFC("사무실용 건축물"),
  CMR("상업용 건축물"),
  IND("공업용 건축물"),
  HSP("병원"),
  SCH("학교"),
  ETC("기타");

  private final String value;

  public static DetailCtrType from(String type) {
    return Arrays.stream(values())
      .filter(role -> role.value.equals(type))
      .findFirst()
      .orElseThrow(() -> new CustomCommonException(ErrorCode.BUSINESS_TYPE_INVALID));
  }
}
