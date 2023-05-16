package snust.sbsp.project.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {

  MANAGER("프로젝트 관리자"),
  EDITABLE("참여 권한자"),
  READABLE("열람 권한자"),
  PENDING("참여승인 대기자");

  private final String value;
}
