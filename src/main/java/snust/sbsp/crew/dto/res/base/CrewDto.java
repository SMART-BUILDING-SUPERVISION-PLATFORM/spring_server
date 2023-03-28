package snust.sbsp.crew.dto.res.base;

import lombok.Getter;
import snust.sbsp.crew.domain.Crew;

@Getter
public class CrewDto {
  private final Long id;
  private final String name;
  private final String email;
  private final String phone;

  private final boolean isPending;

  private final RoleDto role;

  public CrewDto(Crew crew) {
    this.id = crew.getId();
    this.name = crew.getName();
    this.email = crew.getEmail();
    this.phone = crew.getPhone();
    this.isPending = crew.isPending();
    this.role = new RoleDto(crew.getRole());
  }
}
