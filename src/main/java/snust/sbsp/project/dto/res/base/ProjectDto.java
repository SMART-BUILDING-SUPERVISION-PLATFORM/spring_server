package snust.sbsp.project.dto.res.base;

import lombok.Getter;
import snust.sbsp.project.domain.Project;

import java.time.LocalDate;

@Getter
public class ProjectDto {
  private final Long id;
  private final String name;
  private final LocalDate startDate;
  private final LocalDate endDate;
  private final int processRate;
  private final String thumbnailUrl;
  private final String floorUrl;

  public ProjectDto(Project project) {
    this.id = project.getId();
    this.name = project.getName();
    this.startDate = project.getStartDate();
    this.endDate = project.getEndDate();
    this.processRate = project.getProcessRate();
    this.thumbnailUrl = project.getThumbnailUrl();
    this.floorUrl = project.getFloorUrl();
  }
}
