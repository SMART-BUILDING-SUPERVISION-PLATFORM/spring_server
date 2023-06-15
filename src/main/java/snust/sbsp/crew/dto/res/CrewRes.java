package snust.sbsp.crew.dto.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import snust.sbsp.company.dto.res.base.CompanyDto;
import snust.sbsp.crew.domain.Crew;
import snust.sbsp.crew.dto.res.base.CrewDto;
import snust.sbsp.project.dto.res.base.ProjectDto;

import java.util.List;

@Getter
public class CrewRes extends CrewDto {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final CompanyDto company;
  
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private final List<ProjectDto> projectList;

  @Builder
  public CrewRes(
    Crew crew,
    CompanyDto company,
    List<ProjectDto> projectList
  ) {
    super(crew);
    this.company = company;
    this.projectList = projectList;
  }
}
