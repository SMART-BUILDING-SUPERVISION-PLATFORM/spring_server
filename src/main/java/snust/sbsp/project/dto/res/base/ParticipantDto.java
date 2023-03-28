package snust.sbsp.project.dto.res.base;

import lombok.Getter;
import snust.sbsp.project.domain.Participant;

@Getter
public class ParticipantDto {

  private final Long id;

  private final String name;
  
  private final RoleDto role;

  public ParticipantDto(Participant participant) {
    this.id = participant.getId();
    this.name = participant.getCrew().getName();
    this.role = new RoleDto(participant.getRole());
  }
}
