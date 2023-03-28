package snust.sbsp.project.domain.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {

  MANAGER("매니저"),
  EDITABLE("edit"),
  READABLE("read only");

  private final String value;
}
