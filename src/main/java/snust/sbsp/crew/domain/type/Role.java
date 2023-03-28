package snust.sbsp.crew.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

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
}