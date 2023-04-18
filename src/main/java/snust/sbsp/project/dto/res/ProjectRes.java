package snust.sbsp.project.dto.res;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import snust.sbsp.project.domain.Project;
import snust.sbsp.project.dto.res.base.ParticipantDto;
import snust.sbsp.project.dto.res.base.ProjectDto;

import java.util.List;

@Getter
@Builder
public class ProjectRes extends ProjectDto {

  @JsonIgnore
  private Project project;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<ParticipantDto> participantList;

  public ProjectRes(
    Project project,
    List<ParticipantDto> participantList
  ) {
    super(project);
    this.participantList = participantList;
  }
}
