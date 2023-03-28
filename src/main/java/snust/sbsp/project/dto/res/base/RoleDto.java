package snust.sbsp.project.dto.res.base;

import lombok.Getter;
import snust.sbsp.project.domain.type.Role;

@Getter
public class RoleDto {

  private final Role attr;

  private final String value;

  public RoleDto(Role role) {
    this.attr = role;
    this.value = role.getValue();
  }
}
