package snust.sbsp.project.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
}
