package snust.sbsp.crew.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import snust.sbsp.common.exception.CustomCommonException;
import snust.sbsp.common.exception.ErrorCode;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum Role {
  
  SERVICE_ADMIN("서비스 관리자"),
  COMPANY_ADMIN("관리자"),
  ORDER("발주처"),
  SUPERVISOR("감리사"),
  CONSTRUCTION("건설사"),
  DESIGN("설계사");

  private final String value;

  public static Role from(String type) {
    return Arrays.stream(values())
            .filter(role -> role.value.equals(type))
            .findFirst()
            .orElseThrow(() -> new CustomCommonException(ErrorCode.BUSINESS_TYPE_INVALID));
  }
}